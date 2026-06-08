package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.mapper.SysTaskMapper;
import com.qhiot.survey.service.DataScopeService;
import com.qhiot.survey.service.MessageCenterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysTaskServiceImpl 单元测试")
class SysTaskServiceImplTest {

    @Mock
    private SysTaskMapper sysTaskMapper;

    @Mock
    private MessageCenterService messageCenterService;

    @Mock
    private DataScopeService dataScopeService;

    @InjectMocks
    private SysTaskServiceImpl sysTaskService;

    private SysTask activeTask() {
        SysTask task = new SysTask();
        task.setId(1L);
        task.setTaskName("亳州排口勘察任务-01");
        task.setProjectId(10L);
        task.setPlotCode("PLOT-001");
        task.setPriority(1);
        return task;
    }

    @BeforeEach
    void setUp() {
        sysTaskService = Mockito.spy(sysTaskService);
        // Mock standard save and update methods in ServiceImpl to avoid DB dependency
        doReturn(true).when(sysTaskService).save(any(SysTask.class));
        doReturn(true).when(sysTaskService).updateById(any(SysTask.class));
        doReturn(true).when(sysTaskService).removeById(anyLong());
        when(dataScopeService.canAccessProject(anyLong())).thenReturn(true);
        when(dataScopeService.canAccessTask(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("createTask - 创建待指派任务不触发通知")
    void testCreateTaskWithoutAssignee() {
        SysTask task = activeTask();
        boolean success = sysTaskService.createTask(task);

        assertTrue(success);
        assertNotNull(task.getCreateTime());
        assertEquals(0, task.getStatus()); // 无执行人，默认为 0
        verify(messageCenterService, never()).createSystemMessage(any(), any(), any(), any());
    }

    @Test
    @DisplayName("createTask - 创建已指派任务自动触发通知并更新状态")
    void testCreateTaskWithAssignee() {
        SysTask task = activeTask();
        task.setAssigneeId(200L); // 直接指派执行人

        boolean success = sysTaskService.createTask(task);

        assertTrue(success);
        assertNotNull(task.getCreateTime());
        assertEquals(1, task.getStatus()); // 有执行人，自动设定状态为 1 (进行中)
        
        // 验证系统通知是否正常发送
        verify(messageCenterService, times(1)).createSystemMessage(
                eq("收到新勘察指派任务"),
                contains("亳州排口勘察任务-01"),
                eq("audit_reminder"),
                eq(200L)
        );
    }

    @Test
    @DisplayName("updateTask - 变更指派执行人会触发新的通知")
    void testUpdateTaskWithNewAssignee() {
        SysTask oldTask = activeTask();
        oldTask.setAssigneeId(200L);

        SysTask newTask = activeTask();
        newTask.setAssigneeId(300L); // 指派给新执行人

        doReturn(oldTask).when(sysTaskService).getById(1L);

        boolean success = sysTaskService.updateTask(newTask);

        assertTrue(success);
        verify(messageCenterService, times(1)).createSystemMessage(
                eq("收到新勘察指派任务"),
                any(),
                eq("audit_reminder"),
                eq(300L)
        );
    }

    @Test
    @DisplayName("assignTask - 重新指派任务会更新执行人和状态，并触发通知")
    void testAssignTask() {
        SysTask task = activeTask();
        doReturn(task).when(sysTaskService).getById(1L);

        boolean success = sysTaskService.assignTask(1L, 500L);

        assertTrue(success);
        assertEquals(500L, task.getAssigneeId());
        assertEquals(1, task.getStatus()); // 进行中
        
        verify(messageCenterService, times(1)).createSystemMessage(
                eq("收到新勘察指派任务"),
                any(),
                eq("audit_reminder"),
                eq(500L)
        );
    }

    @Test
    @DisplayName("changeTaskStatus - 状态修改成功")
    void testChangeTaskStatus() {
        SysTask task = activeTask();
        doReturn(task).when(sysTaskService).getById(1L);

        boolean success = sysTaskService.changeTaskStatus(1L, 2); // 2已完成

        assertTrue(success);
        assertEquals(2, task.getStatus());
    }

    @Test
    @DisplayName("getTaskById - 任务不存在时抛出 BusinessException")
    void testGetTaskByIdNotFound() {
        doReturn(null).when(sysTaskService).getById(999L);
        when(dataScopeService.canAccessTask(999L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> sysTaskService.getTaskById(999L));
    }
}
