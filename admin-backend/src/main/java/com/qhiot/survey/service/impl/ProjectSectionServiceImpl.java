package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.enums.ResultStatus;
import com.qhiot.survey.common.enums.SurveyPointStatus;
import com.qhiot.survey.common.enums.YesNo;
import com.qhiot.survey.entity.ProjectSection;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.ProjectSectionMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.service.ProjectSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标段管理服务实现类
 */
@Service
public class ProjectSectionServiceImpl extends ServiceImpl<ProjectSectionMapper, ProjectSection> implements ProjectSectionService {

    @Override
    public Page<ProjectSection> listByPage(Long projectId, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ProjectSection> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(ProjectSection::getProjectId, projectId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ProjectSection::getSectionName, keyword);
        }
        wrapper.orderByDesc(ProjectSection::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<ProjectSection> listByProjectId(Long projectId) {
        return lambdaQuery()
                .eq(ProjectSection::getProjectId, projectId)
                .eq(ProjectSection::getStatus, YesNo.YES.getCode())
                .orderByAsc(ProjectSection::getSectionCode)
                .list();
    }

    @Override
    public ProjectSection createSection(ProjectSection section) {
        // 检查标段编号是否重复
        Long count = lambdaQuery()
                .eq(ProjectSection::getSectionCode, section.getSectionCode())
                .count();
        if (count > 0) {
            throw new BusinessException("标段编号已存在");
        }
        save(section);
        return section;
    }

    @Override
    public ProjectSection updateSection(Long id, ProjectSection section) {
        ProjectSection existing = getById(id);
        if (existing == null) {
            throw new BusinessException("标段不存在");
        }
        section.setId(id);
        updateById(section);
        return getById(id);
    }

    @Override
    public void deleteSection(Long id) {
        ProjectSection existing = getById(id);
        if (existing == null) {
            throw new BusinessException("标段不存在");
        }
        removeById(id);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        ProjectSection existing = getById(id);
        if (existing == null) {
            throw new BusinessException("标段不存在");
        }
        ProjectSection update = new ProjectSection();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
    }

    @Autowired
    private SurveyPointMapper surveyPointMapper;

    @Autowired
    private SurveyResultMapper surveyResultMapper;

    @Override
    public void setManager(Long id, Long managerId) {
        ProjectSection existing = getById(id);
        if (existing == null) {
            throw new BusinessException("标段不存在");
        }
        ProjectSection update = new ProjectSection();
        update.setId(id);
        update.setManagerId(managerId);
        updateById(update);
    }

    @Override
    @Transactional
    public void bindPoints(Long sectionId, List<Long> pointIds) {
        ProjectSection existing = getById(sectionId);
        if (existing == null) {
            throw new BusinessException("标段不存在");
        }
        for (Long pointId : pointIds) {
            SurveyPoint point = surveyPointMapper.selectById(pointId);
            if (point != null) {
                point.setSectionId(sectionId);
                surveyPointMapper.updateById(point);
            }
        }
    }

    @Override
    @Transactional
    public void unbindPoints(Long sectionId, List<Long> pointIds) {
        for (Long pointId : pointIds) {
            SurveyPoint point = surveyPointMapper.selectById(pointId);
            if (point != null && sectionId.equals(point.getSectionId())) {
                point.setSectionId(null);
                surveyPointMapper.updateById(point);
            }
        }
    }

    @Override
    public Object getStatistics(Long sectionId) {
        ProjectSection section = getById(sectionId);
        if (section == null) {
            throw new BusinessException("标段不存在");
        }

        // 统计该标段下的点位
        long totalPoints = surveyPointMapper.selectCount(
                new LambdaQueryWrapper<SurveyPoint>().eq(SurveyPoint::getSectionId, sectionId)
        );
        long surveyedPoints = surveyPointMapper.selectCount(
                new LambdaQueryWrapper<SurveyPoint>()
                        .eq(SurveyPoint::getSectionId, sectionId)
                        .ne(SurveyPoint::getStatus, SurveyPointStatus.PENDING.getCode())
        );
        
        // 获取该标段下所有点位ID，然后统计待审核结果
        List<SurveyPoint> points = surveyPointMapper.selectList(
                new LambdaQueryWrapper<SurveyPoint>()
                        .eq(SurveyPoint::getSectionId, sectionId)
                        .select(SurveyPoint::getId)
        );
        List<Long> pointIds = points.stream().map(SurveyPoint::getId).toList();
        
        long pendingAudit = 0;
        if (!pointIds.isEmpty()) {
            pendingAudit = surveyResultMapper.selectCount(
                    new LambdaQueryWrapper<SurveyResult>()
                            .in(SurveyResult::getPointId, pointIds)
                            .eq(SurveyResult::getResultStatus, ResultStatus.PENDING_AUDIT.getCode())
            );
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPoints", totalPoints);
        stats.put("surveyedPoints", surveyedPoints);
        stats.put("completionRate", totalPoints > 0 ? (double) surveyedPoints / totalPoints * 100 : 0);
        stats.put("pendingAudit", pendingAudit);

        return stats;
    }

    @Override
    public void toggleKeyArea(Long id, Integer isKeyArea) {
        ProjectSection existing = getById(id);
        if (existing == null) {
            throw new BusinessException("标段不存在");
        }
        ProjectSection update = new ProjectSection();
        update.setId(id);
        update.setIsKeyArea(isKeyArea);
        updateById(update);
    }

    @Override
    public Map<String, Object> getAuditBacklog(Long sectionId) {
        ProjectSection section = getById(sectionId);
        if (section == null) {
            throw new BusinessException("标段不存在");
        }

        // 获取该标段下所有点位ID
        List<SurveyPoint> points = surveyPointMapper.selectList(
                new LambdaQueryWrapper<SurveyPoint>()
                        .eq(SurveyPoint::getSectionId, sectionId)
                        .select(SurveyPoint::getId)
        );
        List<Long> pointIds = points.stream().map(SurveyPoint::getId).toList();

        Map<String, Object> backlog = new HashMap<>();
        if (pointIds.isEmpty()) {
            backlog.put("totalPending", 0);
            backlog.put("pendingOver3Days", 0);
            backlog.put("pendingOver7Days", 0);
            backlog.put("byAuditor", new HashMap<>());
            return backlog;
        }

        // 统计待审核总数
        long totalPending = surveyResultMapper.selectCount(
                new LambdaQueryWrapper<SurveyResult>()
                        .in(SurveyResult::getPointId, pointIds)
                        .eq(SurveyResult::getResultStatus, ResultStatus.PENDING_AUDIT.getCode())
        );

        // 统计超过3天未审核的
        long pendingOver3Days = surveyResultMapper.selectCount(
                new LambdaQueryWrapper<SurveyResult>()
                        .in(SurveyResult::getPointId, pointIds)
                        .eq(SurveyResult::getResultStatus, ResultStatus.PENDING_AUDIT.getCode())
                        .lt(SurveyResult::getSubmitTime, java.time.LocalDateTime.now().minusDays(3))
        );

        // 统计超过7天未审核的
        long pendingOver7Days = surveyResultMapper.selectCount(
                new LambdaQueryWrapper<SurveyResult>()
                        .in(SurveyResult::getPointId, pointIds)
                        .eq(SurveyResult::getResultStatus, ResultStatus.PENDING_AUDIT.getCode())
                        .lt(SurveyResult::getSubmitTime, java.time.LocalDateTime.now().minusDays(7))
        );

        backlog.put("totalPending", totalPending);
        backlog.put("pendingOver3Days", pendingOver3Days);
        backlog.put("pendingOver7Days", pendingOver7Days);

        return backlog;
    }
}
