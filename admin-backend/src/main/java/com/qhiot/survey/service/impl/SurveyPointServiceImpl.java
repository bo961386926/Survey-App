package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.enums.SurveyPointStatus;
import com.qhiot.survey.common.enums.YesNo;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.service.SurveyPointService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SurveyPointServiceImpl extends ServiceImpl<SurveyPointMapper, SurveyPoint> implements SurveyPointService {

    @Autowired
    private SurveyResultMapper surveyResultMapper;

    @Override
    public List<SurveyPoint> getPointsByProjectId(Long projectId) {
        return lambdaQuery()
                .eq(SurveyPoint::getProjectId, projectId)
                .eq(SurveyPoint::getIsDeleted, YesNo.NO.getCode())
                .orderByAsc(SurveyPoint::getPointCode)
                .list();
    }

    @Override
    @Transactional
    public SurveyPoint createPoint(SurveyPoint point) {
        // 检查点位编号是否重复
        Long count = lambdaQuery()
                .eq(SurveyPoint::getPointCode, point.getPointCode())
                .eq(SurveyPoint::getIsDeleted, YesNo.NO.getCode())
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
        return lambdaQuery()
                .eq(SurveyPoint::getStatus, status)
                .eq(SurveyPoint::getIsDeleted, YesNo.NO.getCode())
                .list();
    }

    @Override
    @Transactional
    public boolean batchCreatePoints(List<SurveyPoint> points) {
        return saveBatch(points);
    }

    @Override
    public Page<SurveyPoint> listByPage(Long projectId, Long sectionId, String keyword, Integer status, Integer pageNum, Integer pageSize) {
        Page<SurveyPoint> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SurveyPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyPoint::getIsDeleted, YesNo.NO.getCode());
        
        if (projectId != null) {
            wrapper.eq(SurveyPoint::getProjectId, projectId);
        }
        if (sectionId != null) {
            wrapper.eq(SurveyPoint::getSectionId, sectionId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SurveyPoint::getPointCode, keyword)
                    .or().like(SurveyPoint::getPointName, keyword));
        }
        if (status != null) {
            wrapper.eq(SurveyPoint::getStatus, status);
        }
        wrapper.orderByAsc(SurveyPoint::getPointCode);
        
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file, Long projectId) {
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
        for (Long pointId : pointIds) {
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
            SurveyPoint point = getById(pointId);
            if (point != null) {
                point.setOutfallType(outfallType);
                updateById(point);
            }
        }
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