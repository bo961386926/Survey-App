package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.service.SysTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("SysTaskController 单元测试")
class SysTaskControllerTest {

    @Mock
    private SysTaskService sysTaskService;

    @InjectMocks
    private SysTaskController sysTaskController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SysTask activeTask() {
        SysTask task = new SysTask();
        task.setId(1L);
        task.setTaskName("亳州排口勘察任务-01");
        task.setProjectId(10L);
        task.setPlotCode("PLOT-001");
        task.setPriority(1);
        task.setStatus(1); // 进行中
        return task;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysTaskController).build();
    }

    @Test
    @DisplayName("分页查询任务列表 - 返回 200")
    void testQueryTaskPage() throws Exception {
        Page<SysTask> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(java.util.Collections.singletonList(activeTask()));

        when(sysTaskService.getTaskPage(anyLong(), anyLong(), anyInt(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/task/page")
                        .param("projectId", "10")
                        .param("assigneeId", "200")
                        .param("status", "1")
                        .param("keyword", "亳州")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].taskName").value("亳州排口勘察任务-01"));
    }

    @Test
    @DisplayName("获取任务详情 - 返回 200")
    void testQueryTaskById() throws Exception {
        SysTask task = activeTask();
        when(sysTaskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskName").value("亳州排口勘察任务-01"));
    }

    @Test
    @DisplayName("创建勘察任务 - 成功时返回 200")
    void testCreateTaskSuccess() throws Exception {
        SysTask task = activeTask();
        when(sysTaskService.createTask(any(SysTask.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/task/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysTaskService, times(1)).createTask(any(SysTask.class));
    }

    @Test
    @DisplayName("更新勘察任务 - 成功时返回 200")
    void testUpdateTaskSuccess() throws Exception {
        SysTask task = activeTask();
        when(sysTaskService.updateTask(any(SysTask.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/task/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysTaskService, times(1)).updateTask(any(SysTask.class));
    }

    @Test
    @DisplayName("变更任务状态 - 成功时返回 200")
    void testChangeStatusSuccess() throws Exception {
        when(sysTaskService.changeTaskStatus(eq(1L), eq(2))).thenReturn(true);

        mockMvc.perform(put("/api/v1/task/1/status")
                        .param("status", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysTaskService, times(1)).changeTaskStatus(eq(1L), eq(2));
    }

    @Test
    @DisplayName("指派任务 - 成功时返回 200")
    void testAssignTaskSuccess() throws Exception {
        when(sysTaskService.assignTask(eq(1L), eq(300L))).thenReturn(true);

        mockMvc.perform(put("/api/v1/task/1/assign")
                        .param("assigneeId", "300")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysTaskService, times(1)).assignTask(eq(1L), eq(300L));
    }

    @Test
    @DisplayName("删除任务 - 成功时返回 200")
    void testDeleteTaskSuccess() throws Exception {
        when(sysTaskService.deleteTask(eq(1L))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysTaskService, times(1)).deleteTask(eq(1L));
    }
}
