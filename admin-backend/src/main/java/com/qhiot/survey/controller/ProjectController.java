package com.qhiot.survey.controller;

import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.dto.ProjectCreateRequest;
import com.qhiot.survey.dto.ProjectQueryRequest;
import com.qhiot.survey.entity.Project;
import com.qhiot.survey.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目管理控制器
 */
@Tag(name = "项目管理")
@RestController
@RequestMapping(value = "/api/v1/project", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "分页查询项目列表")
    @GetMapping(value = "/page", produces = "application/json;charset=UTF-8")
    public Result<PageResult<Project>> queryProjectPage(ProjectQueryRequest request) {
        PageResult<Project> result = projectService.queryProjectPage(request);
        return Result.success(result);
    }

    @Operation(summary = "获取项目详情")
    @GetMapping(value = "/{id}", produces = "application/json;charset=UTF-8")
    public Result<Project> getProjectDetail(@PathVariable Long id) {
        Project project = projectService.getProjectDetail(id);
        return Result.success(project);
    }

    @Operation(summary = "创建项目")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createProject(@RequestBody ProjectCreateRequest request) {
        boolean success = projectService.createProject(request);
        return success ? Result.success() : Result.error("创建项目失败");
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateProject(@PathVariable Long id, @RequestBody ProjectCreateRequest request) {
        boolean success = projectService.updateProject(id, request);
        return success ? Result.success() : Result.error("更新项目失败");
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteProject(@PathVariable Long id) {
        boolean success = projectService.deleteProject(id);
        return success ? Result.success() : Result.error("删除项目失败");
    }

    @Operation(summary = "变更项目状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @Parameter(description = "目标状态: 0草稿 1进行中 2已暂停 3已完成 4已归档")
            @RequestParam Integer targetStatus) {
        boolean success = projectService.changeStatus(id, targetStatus);
        return success ? Result.success() : Result.error("变更状态失败");
    }

    @Operation(summary = "获取项目统计信息")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getProjectStatistics(@PathVariable Long id) {
        Map<String, Object> statistics = projectService.getProjectStatistics(id);
        return Result.success(statistics);
    }

    @Operation(summary = "归档项目")
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> archiveProject(@PathVariable Long id) {
        boolean success = projectService.archiveProject(id);
        return success ? Result.success() : Result.error("归档项目失败");
    }

    @Operation(summary = "恢复项目（取消归档）")
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> restoreProject(@PathVariable Long id) {
        boolean success = projectService.restoreProject(id);
        return success ? Result.success() : Result.error("恢复项目失败");
    }
}
