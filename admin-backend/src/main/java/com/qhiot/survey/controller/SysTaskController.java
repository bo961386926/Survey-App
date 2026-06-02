package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.service.SysTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 任务指派中心控制器
 */
@Slf4j
@Tag(name = "任务指派中心", description = "勘查指派任务管理相关接口")
@RestController
@RequestMapping(value = "/api/v1/task", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class SysTaskController {

    private final SysTaskService sysTaskService;

    @Operation(summary = "分页查询指派任务列表", description = "分页查询勘察指派任务，支持按项目、指派人、状态、关键词筛选")
    @GetMapping(value = "/page", produces = "application/json;charset=UTF-8")
    public Result<Page<SysTask>> queryTaskPage(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysTask> page = sysTaskService.getTaskPage(projectId, assigneeId, status, keyword, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取指派任务详情", description = "根据任务ID获取详细信息")
    @GetMapping("/{id}")
    public Result<SysTask> queryTaskById(@Parameter(description = "任务ID") @PathVariable Long id) {
        SysTask task = sysTaskService.getTaskById(id);
        return Result.success(task);
    }

    @Operation(summary = "创建勘察任务", description = "创建新的勘察指派任务，需ADMIN或PROJECT_MANAGER角色")
    @PostMapping(value = "/create", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @OperationLog(module = "任务中心", action = "创建任务", description = "创建勘察任务: #task.taskName", riskLevel = 1)
    public Result<Void> createTask(@RequestBody SysTask task) {
        boolean success = sysTaskService.createTask(task);
        return success ? Result.success() : Result.error("创建任务失败");
    }

    @Operation(summary = "更新勘察任务", description = "更新勘察任务信息，需ADMIN或PROJECT_MANAGER角色")
    @PutMapping(value = "/update", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @OperationLog(module = "任务中心", action = "更新任务", description = "更新勘察任务ID: #task.id", riskLevel = 1)
    public Result<Void> updateTask(@RequestBody SysTask task) {
        boolean success = sysTaskService.updateTask(task);
        return success ? Result.success() : Result.error("更新任务失败");
    }

    @Operation(summary = "变更任务状态", description = "变更勘察任务状态：0待分配/1进行中/2已完成/3已逾期/4已终止")
    @PutMapping(value = "/{id}/status", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SURVEYOR')")
    @OperationLog(module = "任务中心", action = "变更任务状态", description = "变更任务状态, ID: #id, 目标状态: #status", riskLevel = 1)
    public Result<Void> changeStatus(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(description = "目标状态: 0待分配 1进行中 2已完成 3已逾期 4已终止")
            @RequestParam Integer status) {
        boolean success = sysTaskService.changeTaskStatus(id, status);
        return success ? Result.success() : Result.error("变更任务状态失败");
    }

    @Operation(summary = "指派任务给采集人员", description = "将勘察任务指派给指定采集人员，需ADMIN或PROJECT_MANAGER角色")
    @PutMapping(value = "/{id}/assign", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @OperationLog(module = "任务中心", action = "指派任务", description = "指派任务ID: #id 给采集人员ID: #assigneeId", riskLevel = 1)
    public Result<Void> assignTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(description = "被指派人用户ID") @RequestParam Long assigneeId) {
        boolean success = sysTaskService.assignTask(id, assigneeId);
        return success ? Result.success() : Result.error("指派任务失败");
    }

    @Operation(summary = "删除勘察任务", description = "删除勘察任务，需ADMIN角色，高风险操作")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "任务中心", action = "删除任务", description = "删除勘察任务ID: #id", riskLevel = 2)
    public Result<Void> deleteTask(@Parameter(description = "任务ID") @PathVariable Long id) {
        boolean success = sysTaskService.deleteTask(id);
        return success ? Result.success() : Result.error("删除任务失败");
    }
}
