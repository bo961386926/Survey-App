package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.ExportTask;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.ExportTaskService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 导出任务控制器
 */
@Tag(name = "导出任务", description = "异步导出任务管理")
@RestController
@RequestMapping("/api/v1/export")
public class ExportTaskController {

    @Autowired
    private ExportTaskService exportTaskService;

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "创建导出任务")
    @PostMapping("/create")
    @OperationLog(module = "导出管理", action = "创建任务", description = "创建导出任务, 名称: #taskName, 类型: #taskType", riskLevel = 0)
    public Result<Long> createTask(@RequestParam String taskName,
                                   @RequestParam String taskType,
                                   @RequestParam(required = false) Long projectId) {
        Long creatorId = getCurrentUserId();
        Long taskId = exportTaskService.createExportTask(taskName, taskType, projectId, creatorId);
        return Result.success(taskId);
    }

    @Operation(summary = "创建单点位PDF导出任务")
    @PostMapping("/create-pdf")
    @OperationLog(module = "导出管理", action = "创建PDF任务", description = "创建单点位PDF导出任务, 点位ID: #pointId", riskLevel = 0)
    public Result<Long> createPdfTask(@RequestParam Long pointId,
                                      @RequestParam(required = false) Long resultId) {
        Long creatorId = getCurrentUserId();
        Long taskId = exportTaskService.createPdfExportTask(pointId, resultId, creatorId);
        return Result.success(taskId);
    }

    @Operation(summary = "获取我的导出任务列表")
    @GetMapping("/list")
    public Result<List<ExportTask>> getTaskList() {
        Long userId = getCurrentUserId();
        return Result.success(exportTaskService.getUserTasks(userId));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/detail/{taskId}")
    public Result<ExportTask> getTaskDetail(@PathVariable Long taskId) {
        return Result.success(exportTaskService.getTaskDetail(taskId));
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user.getId();
    }
}
