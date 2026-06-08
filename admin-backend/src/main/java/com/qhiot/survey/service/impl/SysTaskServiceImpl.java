package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.ResultCode;
import com.qhiot.survey.common.util.SecurityUtils;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.entity.Project;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.mapper.SysTaskMapper;
import com.qhiot.survey.service.DataScopeService;
import com.qhiot.survey.service.MessageCenterService;
import com.qhiot.survey.service.SysTaskService;
import com.qhiot.survey.service.ProjectService;
import com.qhiot.survey.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 勘察指派任务服务实现类
 */
@Slf4j
@Service
public class SysTaskServiceImpl extends ServiceImpl<SysTaskMapper, SysTask> implements SysTaskService {

    @Autowired
    private MessageCenterService messageCenterService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ProjectService projectService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private SysUserService sysUserService;

    @Autowired
    private DataScopeService dataScopeService;

    @Override
    public Page<SysTask> getTaskPage(Long projectId, Long assigneeId, Integer status, String category, String keyword, int pageNum, int pageSize) {
        Page<SysTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTask> wrapper = new LambdaQueryWrapper<>();
        applyTaskScope(wrapper, projectId);

        if (projectId != null) {
            wrapper.eq(SysTask::getProjectId, projectId);
        }
        if (assigneeId != null) {
            wrapper.eq(SysTask::getAssigneeId, assigneeId);
        }
        if (status != null) {
            wrapper.eq(SysTask::getStatus, status);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(SysTask::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysTask::getTaskName, keyword)
                    .or().like(SysTask::getPlotCode, keyword));
        }

        wrapper.orderByDesc(SysTask::getCreateTime);
        Page<SysTask> resultPage = page(page, wrapper);

        if (resultPage.getRecords() != null) {
            for (SysTask task : resultPage.getRecords()) {
                populateTaskNames(task);
            }
        }

        return resultPage;
    }

    @Override
    public SysTask getTaskById(Long id) {
        checkTaskAccess(id);
        SysTask task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        populateTaskNames(task);
        return task;
    }

    private void populateTaskNames(SysTask task) {
        if (task.getProjectId() != null) {
            Project project = projectService.getById(task.getProjectId());
            if (project != null) {
                task.setProjectName(project.getProjectName());
            }
        }
        if (task.getAssigneeId() != null) {
            SysUser assignee = sysUserService.getById(task.getAssigneeId());
            if (assignee != null) {
                task.setAssigneeName(assignee.getRealName());
            }
        }
        if (task.getOwnerUserId() != null) {
            SysUser owner = sysUserService.getById(task.getOwnerUserId());
            if (owner != null) {
                task.setOwnerUserName(owner.getRealName() != null ? owner.getRealName() : owner.getUsername());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTask(SysTask task) {
        if (task.getProjectId() != null && !dataScopeService.canAccessProject(task.getProjectId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (task.getOwnerUserId() == null) {
            task.setOwnerUserId(currentUserId);
        }
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        if (task.getStatus() == null) {
            task.setStatus(task.getAssigneeId() != null ? 1 : 0); // 0待分配，1进行中
        }
        boolean saved = save(task);

        // 如果创建任务时直接指定了执行人，发送消息通知
        if (saved && task.getAssigneeId() != null) {
            sendTaskNotification(task);
        }

        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTask(SysTask task) {
        checkTaskAccess(task.getId());
        SysTask oldTask = getById(task.getId());
        if (oldTask == null) {
            throw new BusinessException("任务不存在");
        }
        task.setUpdateTime(LocalDateTime.now());
        boolean updated = updateById(task);

        // 如果指派人变更了，给新指派人发送通知
        if (updated && task.getAssigneeId() != null && !task.getAssigneeId().equals(oldTask.getAssigneeId())) {
            sendTaskNotification(task);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeTaskStatus(Long id, Integer status) {
        checkTaskAccess(id);
        SysTask task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        task.setStatus(status);
        task.setUpdateTime(LocalDateTime.now());
        return updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignTask(Long id, Long assigneeId) {
        checkTaskAccess(id);
        SysTask task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        task.setAssigneeId(assigneeId);
        task.setStatus(1); // 进行中
        task.setUpdateTime(LocalDateTime.now());
        boolean assigned = updateById(task);

        if (assigned && assigneeId != null) {
            sendTaskNotification(task);
        }

        return assigned;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(Long id) {
        checkTaskAccess(id);
        SysTask task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return removeById(id);
    }

    private void sendTaskNotification(SysTask task) {
        try {
            String title = "收到新勘察指派任务";
            String content = String.format("您被指派了一项新的勘察任务: [%s]，项目ID: %d，请尽快前往移动端工作台查看并执行！", 
                    task.getTaskName(), task.getProjectId());
            messageCenterService.createSystemMessage(title, content, "audit_reminder", task.getAssigneeId());
            log.info("任务指派通知已发送给用户: {}", task.getAssigneeId());
        } catch (Exception e) {
            log.error("发送指派任务通知失败", e);
        }
    }

    private void applyTaskScope(LambdaQueryWrapper<SysTask> wrapper, Long requestedProjectId) {
        if (dataScopeService.isSystemAdmin()) {
            return;
        }
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> projectIds = dataScopeService.getAccessibleProjectIds();
        wrapper.and(w -> {
            boolean hasProjectScope = projectIds != null && !projectIds.isEmpty();
            boolean hasUserScope = userId != null;
            boolean appended = false;
            if (hasProjectScope) {
                w.in(SysTask::getProjectId, projectIds);
                appended = true;
            }
            if (hasUserScope) {
                if (appended) {
                    w.or();
                }
                w.eq(SysTask::getAssigneeId, userId)
                        .or()
                        .eq(SysTask::getOwnerUserId, userId);
                appended = true;
            }
            if (!appended) {
                w.eq(SysTask::getId, -1L);
            }
        });
    }

    private void checkTaskAccess(Long taskId) {
        if (!dataScopeService.canAccessTask(taskId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
