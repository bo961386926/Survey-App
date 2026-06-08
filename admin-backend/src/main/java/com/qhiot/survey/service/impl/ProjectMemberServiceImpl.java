package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.ResultCode;
import com.qhiot.survey.entity.ProjectMember;
import com.qhiot.survey.mapper.ProjectMemberMapper;
import com.qhiot.survey.service.DataScopeService;
import com.qhiot.survey.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目成员服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember> implements ProjectMemberService {

    private final DataScopeService dataScopeService;

    @Override
    @Transactional
    public boolean addMember(Long projectId, Long userId, String role) {
        checkProjectAccess(projectId);
        // 检查是否已存在
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            log.warn("用户已是项目成员: projectId={}, userId={}", projectId, userId);
            return false;
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        member.setStatus(1);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        return this.save(member);
    }

    @Override
    @Transactional
    public int batchAddMembers(Long projectId, List<Long> userIds, String role) {
        checkProjectAccess(projectId);
        int count = 0;
        List<ProjectMember> members = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Long userId : userIds) {
            // 检查是否已存在
            LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId);
            if (this.count(queryWrapper) == 0) {
                ProjectMember member = new ProjectMember();
                member.setProjectId(projectId);
                member.setUserId(userId);
                member.setRole(role);
                member.setStatus(1);
                member.setCreateTime(now);
                member.setUpdateTime(now);
                members.add(member);
                count++;
            }
        }
        
        if (!members.isEmpty()) {
            this.saveBatch(members);
        }
        return count;
    }

    @Override
    @Transactional
    public boolean removeMember(Long projectId, Long userId) {
        checkProjectAccess(projectId);
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getUserId, userId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<ProjectMember> getProjectMembers(Long projectId) {
        checkProjectAccess(projectId);
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getStatus, 1)
                    .orderByAsc(ProjectMember::getRole)
                    .orderByDesc(ProjectMember::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean updateMemberRole(Long projectId, Long userId, String newRole) {
        checkProjectAccess(projectId);
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getUserId, userId);
        ProjectMember member = this.getOne(queryWrapper);
        if (member == null) {
            log.warn("用户不是项目成员: projectId={}, userId={}", projectId, userId);
            return false;
        }
        member.setRole(newRole);
        member.setUpdateTime(LocalDateTime.now());
        return this.updateById(member);
    }

    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        checkProjectAccess(projectId);
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getUserId, userId)
                    .eq(ProjectMember::getStatus, 1);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public String getUserRoleInProject(Long projectId, Long userId) {
        checkProjectAccess(projectId);
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getUserId, userId)
                    .eq(ProjectMember::getStatus, 1);
        ProjectMember member = this.getOne(queryWrapper);
        return member != null ? member.getRole() : null;
    }

    private void checkProjectAccess(Long projectId) {
        if (!dataScopeService.canAccessProject(projectId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
