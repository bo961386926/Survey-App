package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.ExportTask;
import com.qhiot.survey.service.ExportTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 导出任务控制器
 */
@Tag(name = "导出任务", description = "异步导出任务管理")
@RestController
@RequestMapping("/api/export")
public class ExportTaskController {

    @Autowired
    private ExportTaskService exportTaskService;

    @Operation(summary = "创建导出任务")
    @PostMapping("/create")
    public Result<Long> createTask(@RequestParam String taskName,
                                   @RequestParam String taskType,
                                   @RequestParam(required = false) Long projectId) {
        // 实际应该从SecurityContext获取当前用户ID
        Long creatorId = 1L;
        Long taskId = exportTaskService.createExportTask(taskName, taskType, projectId, creatorId);
        return Result.success(taskId);
    }

    @Operation(summary = "创建单点位PDF导出任务")
    @PostMapping("/create-pdf")
    public Result<Long> createPdfTask(@RequestParam Long pointId,
                                      @RequestParam(required = false) Long resultId) {
        // 实际应该从SecurityContext获取当前用户ID
        Long creatorId = 1L;
        Long taskId = exportTaskService.createPdfExportTask(pointId, resultId, creatorId);
        return Result.success(taskId);
    }

    @Operation(summary = "获取我的导出任务列表")
    @GetMapping("/list")
    public Result<List<ExportTask>> getTaskList() {
        // 实际应该从SecurityContext获取当前用户ID
        Long userId = 1L;
        return Result.success(exportTaskService.getUserTasks(userId));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/detail/{taskId}")
    public Result<ExportTask> getTaskDetail(@PathVariable Long taskId) {
        return Result.success(exportTaskService.getTaskDetail(taskId));
    }
}
