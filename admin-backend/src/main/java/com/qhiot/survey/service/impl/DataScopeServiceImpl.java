package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qhiot.survey.common.util.SecurityUtils;
import com.qhiot.survey.entity.ProjectMember;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.mapper.ProjectMemberMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SysTaskMapper;
import com.qhiot.survey.security.CollabSecurityService;
import com.qhiot.survey.service.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 基于项目成员、点位负责人、任务负责人/执行人的数据范围实现。
 */
@Service
@RequiredArgsConstructor
public class DataScopeServiceImpl implements DataScopeService {

    private final ProjectMemberMapper projectMemberMapper;
    private final SurveyPointMapper surveyPointMapper;
    private final SysTaskMapper sysTaskMapper;
    private final CollabSecurityService collabSecurityService;

    @Override
    public boolean isSystemAdmin() {
        return SecurityUtils.hasRole("ADMIN");
    }

    @Override
    public List<Long> getAccessibleProjectIds() {
        if (isSystemAdmin()) {
            return null;
        }
        var collabEntry = collabSecurityService.loadCurrentEntry(getPrincipalName());
        if (collabEntry != null) {
            return collabSecurityService.getAuthorizedProjectIds(collabEntry);
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        return projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getUserId, userId)
                        .eq(ProjectMember::getStatus, 1))
                .stream()
                .map(ProjectMember::getProjectId)
                .distinct()
                .toList();
    }

    @Override
    public List<Long> getAccessiblePointIds() {
        if (isSystemAdmin()) {
            return null;
        }
        var collabEntry = collabSecurityService.loadCurrentEntry(getPrincipalName());
        if (collabEntry != null) {
            return collabSecurityService.getAuthorizedPointIds(collabEntry);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canAccessProject(Long projectId) {
        if (projectId == null) {
            return false;
        }
        if (isSystemAdmin()) {
            return true;
        }
        var collabEntry = collabSecurityService.loadCurrentEntry(getPrincipalName());
        if (collabEntry != null) {
            return collabSecurityService.getAuthorizedProjectIds(collabEntry).contains(projectId);
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        return projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .eq(ProjectMember::getStatus, 1)) > 0;
    }

    @Override
    public boolean canAccessPoint(Long pointId) {
        if (pointId == null) {
            return false;
        }
        if (isSystemAdmin()) {
            return true;
        }
        SurveyPoint point = surveyPointMapper.selectById(pointId);
        if (point == null) {
            return false;
        }
        var collabEntry = collabSecurityService.loadCurrentEntry(getPrincipalName());
        if (collabEntry != null) {
            return collabSecurityService.getAuthorizedPointIds(collabEntry).contains(pointId)
                    || collabSecurityService.getAuthorizedProjectIds(collabEntry).contains(point.getProjectId());
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        return canAccessProject(point.getProjectId())
                || userId.equals(point.getAssigneeId())
                || userId.equals(point.getCollectorId());
    }

    @Override
    public boolean canAccessTask(Long taskId) {
        if (taskId == null) {
            return false;
        }
        if (isSystemAdmin()) {
            return true;
        }
        SysTask task = sysTaskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        var collabEntry = collabSecurityService.loadCurrentEntry(getPrincipalName());
        if (collabEntry != null) {
            return collabSecurityService.getAuthorizedProjectIds(collabEntry).contains(task.getProjectId());
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        return canAccessProject(task.getProjectId())
                || userId.equals(task.getAssigneeId())
                || userId.equals(task.getOwnerUserId());
    }

    private String getPrincipalName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
