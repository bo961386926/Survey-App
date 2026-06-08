package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.ResultCode;
import com.qhiot.survey.common.enums.SurveyPointStatus;
import com.qhiot.survey.common.enums.YesNo;
import com.qhiot.survey.common.util.SecurityUtils;
import com.qhiot.survey.dto.SurveyPointDTO;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.service.DataScopeService;
import com.qhiot.survey.service.SurveyPointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SurveyPointServiceImpl extends ServiceImpl<SurveyPointMapper, SurveyPoint> implements SurveyPointService {

    @Autowired
    private SurveyResultMapper surveyResultMapper;

    @Autowired
    private DataScopeService dataScopeService;

    @Override
    public List<SurveyPoint> getPointsByProjectId(Long projectId) {
        if (projectId != null && !dataScopeService.canAccessProject(projectId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return lambdaQuery()
                .eq(SurveyPoint::getProjectId, projectId)
                .orderByAsc(SurveyPoint::getPointCode)
                .list();
    }

    @Override
    public List<SurveyPoint> getAccessiblePoints(Long projectId) {
        LambdaQueryWrapper<SurveyPoint> wrapper = buildScopedPointWrapper(projectId, null, null, null);
        wrapper.orderByAsc(SurveyPoint::getPointCode);
        return list(wrapper);
    }

    @Override
    public SurveyPoint getAccessiblePointById(Long id) {
        checkPointAccess(id);
        SurveyPoint point = getById(id);
        if (point == null) {
            throw new BusinessException("点位不存在");
        }
        return point;
    }

    @Override
    @Transactional
    public SurveyPoint createPoint(SurveyPoint point) {
        checkProjectAccess(point.getProjectId());
        // 检查点位编号是否重复（@TableLogic 自动过滤已删除记录）
        Long count = lambdaQuery()
                .eq(SurveyPoint::getPointCode, point.getPointCode())
                .count();
        if (count > 0) {
            throw new BusinessException("点位编号已存在");
        }
        point.setStatus(SurveyPointStatus.PENDING.getCode());
        point.setIsDeleted(YesNo.NO.getCode());
        save(point);
        return point;
    }

    @Override
    @Transactional
    public SurveyPoint updatePoint(Long id, SurveyPoint point) {
        checkPointAccess(id);
        SurveyPoint existing = getById(id);
        if (existing == null) {
            throw new BusinessException("点位不存在");
        }
        point.setId(id);
        updateById(point);
        return getById(id);
    }

    @Override
    @Transactional
    public void deletePoint(Long id) {
        checkPointAccess(id);
        SurveyPoint existing = getById(id);
        if (existing == null) {
            throw new BusinessException("点位不存在");
        }
        // 逻辑删除
        existing.setIsDeleted(YesNo.YES.getCode());
        updateById(existing);
    }

    @Override
    public List<SurveyPoint> getPointsByStatus(Integer status) {
        return list(buildScopedPointWrapper(null, null, null, status));
    }

    @Override
    @Transactional
    public boolean batchCreatePoints(List<SurveyPoint> points) {
        for (SurveyPoint point : points) {
            checkProjectAccess(point.getProjectId());
        }
        return saveBatch(points);
    }

    @Override
    public Page<SurveyPoint> listByPage(Long projectId, Long sectionId, String keyword, Integer status, Integer pageNum, Integer pageSize) {
        Page<SurveyPoint> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SurveyPoint> wrapper = new LambdaQueryWrapper<>();
        applyPointScope(wrapper, projectId);
        
        if (projectId != null) {
            wrapper.eq(SurveyPoint::getProjectId, projectId);
        }
        if (sectionId != null) {
            wrapper.eq(SurveyPoint::getSectionId, sectionId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SurveyPoint::getPointName, keyword)
                    .or()
                    .like(SurveyPoint::getPointCode, keyword));
        }
        if (status != null) {
            wrapper.eq(SurveyPoint::getStatus, status);
        }
        
        wrapper.orderByDesc(SurveyPoint::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Page<SurveyPointDTO> listByPageWithProject(Long projectId, Long sectionId, String keyword, Integer status, Integer pageNum, Integer pageSize) {
        if (!dataScopeService.isSystemAdmin()) {
            Page<SurveyPoint> pointPage = listByPage(projectId, sectionId, keyword, status, pageNum, pageSize);
            Page<SurveyPointDTO> dtoPage = new Page<>(pageNum, pageSize);
            dtoPage.setTotal(pointPage.getTotal());
            dtoPage.setPages(pointPage.getPages());
            dtoPage.setCurrent(pointPage.getCurrent());
            dtoPage.setSize(pointPage.getSize());
            dtoPage.setRecords(pointPage.getRecords().stream().map(this::toDto).toList());
            return dtoPage;
        }
        Page<SurveyPointDTO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPointPageWithProject(page, projectId, sectionId, keyword, status);
    }

    @Override
    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file, Long projectId) {
        checkProjectAccess(projectId);
        Map<String, Object> result = new HashMap<>();
        List<SurveyPoint> points = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    SurveyPoint point = new SurveyPoint();
                    point.setProjectId(projectId);
                    point.setPointCode(getCellValue(row.getCell(0)));
                    point.setPointName(getCellValue(row.getCell(1)));
                    point.setOutfallType(getCellValue(row.getCell(2)));
                    point.setRegion(getCellValue(row.getCell(3)));
                    
                    // 解析经纬度
                    String lonStr = getCellValue(row.getCell(4));
                    String latStr = getCellValue(row.getCell(5));
                    if (StringUtils.hasText(lonStr)) {
                        point.setLongitude(new BigDecimal(lonStr));
                    }
                    if (StringUtils.hasText(latStr)) {
                        point.setLatitude(new BigDecimal(latStr));
                    }
                    
                    point.setStatus(SurveyPointStatus.PENDING.getCode());
                    point.setIsDeleted(YesNo.NO.getCode());
                    points.add(point);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行导入失败: " + e.getMessage());
                }
            }

            if (!points.isEmpty()) {
                saveBatch(points);
            }

            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errors", errors);
            
        } catch (Exception e) {
            throw new BusinessException("Excel导入失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public void batchAssign(Long projectId, List<Long> pointIds, Long assigneeId) {
        checkProjectAccess(projectId);
        for (Long pointId : pointIds) {
            checkPointAccess(pointId);
            SurveyPoint point = getById(pointId);
            if (point != null && point.getProjectId().equals(projectId)) {
                point.setAssigneeId(assigneeId);
                updateById(point);
            }
        }
    }

    @Override
    @Transactional
    public void invalidatePoint(Long id, String reason) {
        checkPointAccess(id);
        SurveyPoint point = getById(id);
        if (point == null) {
            throw new BusinessException("点位不存在");
        }
        point.setStatus(SurveyPointStatus.INVALIDATED.getCode());
        point.setAbnormalTag(reason);
        updateById(point);
    }

    @Override
    public List<Map<String, Object>> getPointHistory(Long pointId) {
        checkPointAccess(pointId);
        List<Map<String, Object>> history = new ArrayList<>();
        
        // 获取该点位的所有采集结果
        List<SurveyResult> results = surveyResultMapper.selectList(
                new LambdaQueryWrapper<SurveyResult>()
                        .eq(SurveyResult::getPointId, pointId)
                        .orderByDesc(SurveyResult::getVersionNo)
        );
        
        for (SurveyResult result : results) {
            Map<String, Object> record = new HashMap<>();
            record.put("versionNo", result.getVersionNo());
            record.put("status", result.getResultStatus());
            record.put("auditStatus", result.getAuditStatus());
            record.put("auditRemark", result.getAuditRemark());
            record.put("submitTime", result.getSubmitTime());
            record.put("auditTime", result.getAuditTime());
            history.add(record);
        }
        
        return history;
    }

    @Override
    @Transactional
    public void batchSetOutfallType(List<Long> pointIds, String outfallType) {
        for (Long pointId : pointIds) {
            checkPointAccess(pointId);
            SurveyPoint point = getById(pointId);
            if (point != null) {
                point.setOutfallType(outfallType);
                updateById(point);
            }
        }
    }

    private LambdaQueryWrapper<SurveyPoint> buildScopedPointWrapper(Long projectId, Long sectionId, String keyword, Integer status) {
        LambdaQueryWrapper<SurveyPoint> wrapper = new LambdaQueryWrapper<>();
        applyPointScope(wrapper, projectId);
        if (projectId != null) {
            wrapper.eq(SurveyPoint::getProjectId, projectId);
        }
        if (sectionId != null) {
            wrapper.eq(SurveyPoint::getSectionId, sectionId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SurveyPoint::getPointName, keyword)
                    .or()
                    .like(SurveyPoint::getPointCode, keyword));
        }
        if (status != null) {
            wrapper.eq(SurveyPoint::getStatus, status);
        }
        return wrapper;
    }

    private void applyPointScope(LambdaQueryWrapper<SurveyPoint> wrapper, Long requestedProjectId) {
        if (dataScopeService.isSystemAdmin()) {
            return;
        }
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> projectIds = dataScopeService.getAccessibleProjectIds();
        List<Long> pointIds = dataScopeService.getAccessiblePointIds();
        wrapper.and(w -> {
            boolean hasProjectScope = projectIds != null && !projectIds.isEmpty();
            boolean hasPointScope = pointIds != null && !pointIds.isEmpty();
            boolean hasUserScope = userId != null;
            boolean appended = false;
            if (hasProjectScope) {
                w.in(SurveyPoint::getProjectId, projectIds);
                appended = true;
            }
            if (hasPointScope) {
                if (appended) {
                    w.or();
                }
                w.in(SurveyPoint::getId, pointIds);
                appended = true;
            }
            if (hasUserScope) {
                if (appended) {
                    w.or();
                }
                w.eq(SurveyPoint::getAssigneeId, userId)
                        .or()
                        .eq(SurveyPoint::getCollectorId, userId);
                appended = true;
            }
            if (!appended) {
                w.eq(SurveyPoint::getId, -1L);
            }
        });
    }

    private void checkPointAccess(Long pointId) {
        if (!dataScopeService.canAccessPoint(pointId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void checkProjectAccess(Long projectId) {
        if (!dataScopeService.canAccessProject(projectId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    // =========== 地图相关 API 实现 ===========

    @Override
    public List<SurveyPointDTO> getMapPoints(Long projectId, Long sectionId, Integer status) {
        LambdaQueryWrapper<SurveyPoint> wrapper = buildScopedPointWrapper(projectId, sectionId, null, status);
        wrapper.select(SurveyPoint::getId, SurveyPoint::getPointName, SurveyPoint::getPointCode,
                SurveyPoint::getLongitude, SurveyPoint::getLatitude, SurveyPoint::getStatus,
                SurveyPoint::getAbnormalTag, SurveyPoint::getProjectId, SurveyPoint::getOutfallType);
        return list(wrapper).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getMapPointStatistics(Long projectId) {
        List<SurveyPoint> points = getAccessiblePoints(projectId);
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("pending", points.stream().filter(p -> p.getStatus() == 0).count());
        stats.put("draft", points.stream().filter(p -> p.getStatus() == 1).count());
        stats.put("pendingAudit", points.stream().filter(p -> p.getStatus() == 2).count());
        stats.put("passed", points.stream().filter(p -> p.getStatus() == 3).count());
        stats.put("rejected", points.stream().filter(p -> p.getStatus() == 4).count());
        stats.put("archived", points.stream().filter(p -> p.getStatus() == 5).count());
        stats.put("invalid", points.stream().filter(p -> p.getStatus() == 6).count());
        return stats;
    }

    @Override
    public List<SurveyPointDTO> getAbnormalPoints(Long projectId) {
        List<SurveyPoint> points = getAccessiblePoints(projectId);
        return points.stream()
                .filter(p -> p.getAbnormalTag() != null && !p.getAbnormalTag().isEmpty())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private SurveyPointDTO toDto(SurveyPoint point) {
        SurveyPointDTO dto = new SurveyPointDTO();
        BeanUtils.copyProperties(point, dto);
        return dto;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
