package com.qhiot.survey.controller;

import com.qhiot.survey.common.annotation.OperationLog;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目管理控制器
 */
@Slf4j
@Tag(name = "项目管理", description = "项目增删改查、状态管理、归档恢复等接口")
@RestController
@RequestMapping(value = "/api/v1/project", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "分页查询项目列表", description = "支持按项目名称、编号、状态等条件分页查询")
    @GetMapping(value = "/page", produces = "application/json;charset=UTF-8")
    public Result<PageResult<Project>> queryProjectPage(ProjectQueryRequest request) {
        PageResult<Project> result = projectService.queryProjectPage(request);
        return Result.success(result);
    }

    @Operation(summary = "获取项目详情", description = "根据项目ID获取项目详细信息")
    @GetMapping(value = "/{id}", produces = "application/json;charset=UTF-8")
    public Result<Project> getProjectDetail(@PathVariable Long id) {
        log.info("====== [项目管理] 获取项目详情请求 - projectId: {} ======", id);
        Project project = projectService.getProjectDetail(id);
        if (project != null) {
            log.info("====== [项目管理] 获取项目详情成功 - projectName: {} ======", project.getProjectName());
        } else {
            log.warn("====== [项目管理] 项目不存在 - projectId: {} ======", id);
        }
        return Result.success(project);
    }

    @Operation(summary = "创建项目", description = "创建新项目，需ADMIN角色")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目管理", action = "创建", description = "创建项目: #request.projectName", riskLevel = 1)
    public Result<Void> createProject(@RequestBody ProjectCreateRequest request) {
        log.info("====== [项目管理] 创建项目请求 - projectName: {} ======", request.getProjectName());
        
        boolean success = projectService.createProject(request);
        
        if (success) {
            log.info("====== [项目管理] 项目创建成功 - projectName: {} ======", request.getProjectName());
        } else {
            log.error("====== [项目管理] 项目创建失败 - projectName: {} ======", request.getProjectName());
        }
        
        return success ? Result.success() : Result.error("创建项目失败");
    }

    @Operation(summary = "更新项目", description = "更新项目信息，需ADMIN角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目管理", action = "更新", description = "更新项目ID: #id", riskLevel = 1)
    public Result<Project> updateProject(@PathVariable Long id, @RequestBody ProjectCreateRequest request) {
        log.info("====== [项目管理] 更新项目请求 - projectId: {} ======", id);
        boolean success = projectService.updateProject(id, request);
        if (success) {
            log.info("====== [项目管理] 项目更新成功 - projectId: {} ======", id);
            Project updatedProject = projectService.getProjectDetail(id);
            return Result.success(updatedProject);
        } else {
            log.error("====== [项目管理] 项目更新失败 - projectId: {} ======", id);
            return Result.error("更新项目失败");
        }
    }

    @Operation(summary = "删除项目", description = "删除项目，需ADMIN角色，高风险操作")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目管理", action = "删除", description = "删除项目ID: #id", riskLevel = 2)
    public Result<Void> deleteProject(@PathVariable Long id) {
        log.info("====== [项目管理] 删除项目请求 - projectId: {} ======", id);
        try {
            boolean success = projectService.deleteProject(id);
            if (success) {
                log.info("====== [项目管理] 项目删除成功 - projectId: {} ======", id);
                return Result.success();
            } else {
                log.error("====== [项目管理] 项目删除失败 - projectId: {} ======", id);
                return Result.error("删除项目失败");
            }
        } catch (Exception e) {
            log.error("====== [项目管理] 删除项目异常 - projectId: {}, error: {} ======", id, e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "变更项目状态", description = "变更项目状态：0草稿、1进行中、2已暂停、3已完成、4已归档")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目管理", action = "变更状态", description = "变更项目状态, ID: #id, 目标状态: #targetStatus", riskLevel = 1)
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @Parameter(description = "目标状态: 0草稿 1进行中 2已暂停 3已完成 4已归档")
            @RequestParam Integer targetStatus) {
        boolean success = projectService.changeStatus(id, targetStatus);
        return success ? Result.success() : Result.error("变更状态失败");
    }

    @Operation(summary = "获取项目统计信息", description = "获取指定项目下的点位数、勘查完成率等统计数据")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getProjectStatistics(@PathVariable Long id) {
        Map<String, Object> statistics = projectService.getProjectStatistics(id);
        return Result.success(statistics);
    }

    @Operation(summary = "归档项目", description = "将已完成的项目归档，归档后数据只读")
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目管理", action = "归档", description = "归档项目ID: #id", riskLevel = 1)
    public Result<Void> archiveProject(@PathVariable Long id) {
        boolean success = projectService.archiveProject(id);
        return success ? Result.success() : Result.error("归档项目失败");
    }

    @Operation(summary = "恢复项目（取消归档）", description = "取消项目归档状态，恢复为可编辑状态")
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目管理", action = "恢复", description = "恢复项目ID: #id", riskLevel = 1)
    public Result<Void> restoreProject(@PathVariable Long id) {
        boolean success = projectService.restoreProject(id);
        return success ? Result.success() : Result.error("恢复项目失败");
    }
}
