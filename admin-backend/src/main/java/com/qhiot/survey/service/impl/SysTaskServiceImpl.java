package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.mapper.SysTaskMapper;
import com.qhiot.survey.service.MessageCenterService;
import com.qhiot.survey.service.SysTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 勘察指派任务服务实现类
 */
@Slf4j
@Service
public class SysTaskServiceImpl extends ServiceImpl<SysTaskMapper, SysTask> implements SysTaskService {

    @Autowired
    private MessageCenterService messageCenterService;

    @Override
    public Page<SysTask> getTaskPage(Long projectId, Long assigneeId, Integer status, String keyword, int pageNum, int pageSize) {
        Page<SysTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTask> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(SysTask::getProjectId, projectId);
        }
        if (assigneeId != null) {
            wrapper.eq(SysTask::getAssigneeId, assigneeId);
        }
        if (status != null) {
            wrapper.eq(SysTask::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysTask::getTaskName, keyword)
                    .or().like(SysTask::getPlotCode, keyword));
        }

        wrapper.orderByDesc(SysTask::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public SysTask getTaskById(Long id) {
        SysTask task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTask(SysTask task) {
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
}
