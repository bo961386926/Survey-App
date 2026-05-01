package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.dto.ProjectCreateRequest;
import com.qhiot.survey.dto.ProjectQueryRequest;
import com.qhiot.survey.entity.Project;
import com.qhiot.survey.mapper.ProjectMapper;
import com.qhiot.survey.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    /**
     * 项目状态常量
     */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_ARCHIVED = 4;

    @Override
    public PageResult<Project> queryProjectPage(ProjectQueryRequest request) {
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (StringUtils.hasText(request.getProjectName())) {
            queryWrapper.like(Project::getProjectName, request.getProjectName());
        }
        if (StringUtils.hasText(request.getProjectCode())) {
            queryWrapper.like(Project::getProjectCode, request.getProjectCode());
        }
        if (StringUtils.hasText(request.getManager())) {
            queryWrapper.like(Project::getManager, request.getManager());
        }
        if (StringUtils.hasText(request.getRegion())) {
            queryWrapper.eq(Project::getRegion, request.getRegion());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(Project::getStatus, request.getStatus());
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Project::getCreateTime);
        
        // 分页查询
        Page<Project> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Project> result = page(page, queryWrapper);
        
        // 计算总页数
        long totalPages = (result.getTotal() + request.getPageSize() - 1) / request.getPageSize();
        
        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                request.getPageNum(),
                request.getPageSize(),
                (int) totalPages
        );
    }

    @Override
    @Transactional
    public boolean createProject(ProjectCreateRequest request) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setProjectCode(request.getProjectCode());
        project.setManager(request.getManager());
        project.setRegion(request.getRegion());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(STATUS_DRAFT);
        project.setTemplateCount(0);
        project.setPointCount(0);
        project.setCompletedCount(0);
        project.setPendingAuditCount(0);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        
        return save(project);
    }

    @Override
    @Transactional
    public boolean updateProject(Long id, ProjectCreateRequest request) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 已归档的项目不允许修改
        if (project.getStatus() == STATUS_ARCHIVED) {
            throw new RuntimeException("已归档的项目不允许修改");
        }
        
        project.setProjectName(request.getProjectName());
        project.setProjectCode(request.getProjectCode());
        project.setManager(request.getManager());
        project.setRegion(request.getRegion());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdateTime(LocalDateTime.now());
        
        return updateById(project);
    }

    @Override
    @Transactional
    public boolean deleteProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 只有草稿状态的项目可以删除
        if (project.getStatus() != STATUS_DRAFT) {
            throw new RuntimeException("只有草稿状态的项目可以删除");
        }
        
        return removeById(id);
    }

    @Override
    public Project getProjectDetail(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public boolean changeStatus(Long id, Integer targetStatus) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        int currentStatus = project.getStatus();
        
        // 状态机校验
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new RuntimeException("不允许的状态转换: " + currentStatus + " -> " + targetStatus);
        }
        
        project.setStatus(targetStatus);
        project.setUpdateTime(LocalDateTime.now());
        
        return updateById(project);
    }

    /**
     * 检查状态转换是否合法
     * 状态转换规则:
     * - 草稿(0) -> 进行中(1)
     * - 进行中(1) -> 已暂停(2)
     * - 进行中(1) -> 已完成(3)
     * - 已暂停(2) -> 进行中(1)
     * - 已完成(3) -> 已归档(4)
     */
    private boolean isValidTransition(int currentStatus, int targetStatus) {
        return switch (currentStatus) {
            case STATUS_DRAFT -> targetStatus == STATUS_IN_PROGRESS;
            case STATUS_IN_PROGRESS -> targetStatus == STATUS_PAUSED || targetStatus == STATUS_COMPLETED;
            case STATUS_PAUSED -> targetStatus == STATUS_IN_PROGRESS;
            case STATUS_COMPLETED -> targetStatus == STATUS_ARCHIVED;
            default -> false;
        };
    }

    @Override
    public Map<String, Object> getProjectStatistics(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("projectName", project.getProjectName());
        statistics.put("projectCode", project.getProjectCode());
        statistics.put("status", project.getStatus());
        statistics.put("templateCount", project.getTemplateCount());
        statistics.put("pointCount", project.getPointCount());
        statistics.put("completedCount", project.getCompletedCount());
        statistics.put("pendingAuditCount", project.getPendingAuditCount());
        
        // 计算完成率
        if (project.getPointCount() != null && project.getPointCount() > 0) {
            double completionRate = (double) project.getCompletedCount() / project.getPointCount() * 100;
            statistics.put("completionRate", String.format("%.2f%%", completionRate));
        } else {
            statistics.put("completionRate", "0.00%");
        }
        
        return statistics;
    }

    @Override
    @Transactional
    public boolean archiveProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 只有已完成的项目可以归档
        if (project.getStatus() != STATUS_COMPLETED) {
            throw new RuntimeException("只有已完成的项目可以归档");
        }
        
        project.setStatus(STATUS_ARCHIVED);
        project.setUpdateTime(LocalDateTime.now());
        
        return updateById(project);
    }

    @Override
    @Transactional
    public boolean restoreProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 只有已归档的项目可以恢复
        if (project.getStatus() != STATUS_ARCHIVED) {
            throw new RuntimeException("只有已归档的项目可以恢复");
        }
        
        project.setStatus(STATUS_COMPLETED);
        project.setUpdateTime(LocalDateTime.now());
        
        return updateById(project);
    }
}
