package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.entity.ProjectSection;
import com.qhiot.survey.service.ProjectSectionService;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标段管理控制器
 */
@Tag(name = "标段管理", description = "项目标段管理相关接口")
@RestController
@RequestMapping("/api/v1/section")
public class ProjectSectionController {

    @Autowired
    private ProjectSectionService projectSectionService;

    @Operation(summary = "分页查询标段列表", description = "分页查询标段，支持按项目ID和关键词筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_VIEW + "')")
    public Result<Page<ProjectSection>> listByPage(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(projectSectionService.listByPage(projectId, keyword, pageNum, pageSize));
    }

    @Operation(summary = "根据项目ID获取标段列表", description = "获取指定项目下的所有标段列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_VIEW + "')")
    public Result<List<ProjectSection>> listByProjectId(@RequestParam Long projectId) {
        return Result.success(projectSectionService.listByProjectId(projectId));
    }

    @Operation(summary = "获取标段详情", description = "根据标段ID获取详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_VIEW + "')")
    public Result<ProjectSection> getById(@Parameter(description = "标段ID") @PathVariable Long id) {
        return Result.success(projectSectionService.getById(id));
    }

    @Operation(summary = "创建标段", description = "创建新的项目标段")
    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "创建", description = "创建标段: #section.sectionName", riskLevel = 1)
    public Result<ProjectSection> create(@RequestBody ProjectSection section) {
        return Result.success(projectSectionService.createSection(section));
    }

    @Operation(summary = "更新标段", description = "更新标段信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "更新", description = "更新标段ID: #id", riskLevel = 1)
    public Result<ProjectSection> update(@Parameter(description = "标段ID") @PathVariable Long id, @RequestBody ProjectSection section) {
        return Result.success(projectSectionService.updateSection(id, section));
    }

    @Operation(summary = "删除标段", description = "删除标段，高风险操作")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "删除", description = "删除标段ID: #id", riskLevel = 2)
    public Result<Void> delete(@Parameter(description = "标段ID") @PathVariable Long id) {
        projectSectionService.deleteSection(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用标段", description = "切换标段的启用/禁用状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "变更状态", description = "变更标段状态, ID: #id, 目标状态: #status", riskLevel = 1)
    public Result<Void> toggleStatus(@Parameter(description = "标段ID") @PathVariable Long id,
                                       @Parameter(description = "状态：0禁用/1启用") @RequestParam Integer status) {
        projectSectionService.toggleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "设置标段负责人", description = "指定标段的负责人")
    @PutMapping("/{id}/manager")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "设置负责人", description = "设置标段负责人, 标段ID: #id, 负责人ID: #managerId", riskLevel = 1)
    public Result<Void> setManager(@Parameter(description = "标段ID") @PathVariable Long id,
                                     @Parameter(description = "负责人用户ID") @RequestParam Long managerId) {
        projectSectionService.setManager(id, managerId);
        return Result.success();
    }

    @Operation(summary = "绑定点位到标段", description = "将一组点位绑定到指定标段")
    @PostMapping("/{id}/bind-points")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "绑定点位", description = "绑定点位到标段, 标段ID: #id, 点位数量: #pointIds.size()", riskLevel = 0)
    public Result<Void> bindPoints(@Parameter(description = "标段ID") @PathVariable Long id, @RequestBody List<Long> pointIds) {
        projectSectionService.bindPoints(id, pointIds);
        return Result.success();
    }

    @Operation(summary = "解绑点位", description = "将点位从标段解绑")
    @PostMapping("/{id}/unbind-points")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "解绑点位", description = "解绑标段点位, 标段ID: #id, 点位数量: #pointIds.size()", riskLevel = 0)
    public Result<Void> unbindPoints(@Parameter(description = "标段ID") @PathVariable Long id, @RequestBody List<Long> pointIds) {
        projectSectionService.unbindPoints(id, pointIds);
        return Result.success();
    }

    @Operation(summary = "获取标段统计信息", description = "获取标段下的点位数、完成率等统计数据")
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_VIEW + "')")
    public Result<Object> getStatistics(@Parameter(description = "标段ID") @PathVariable Long id) {
        return Result.success(projectSectionService.getStatistics(id));
    }

    @Operation(summary = "标记/取消标记重点区域", description = "设置或取消标段为重点区域")
    @PutMapping("/{id}/key-area")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_EDIT + "')")
    @OperationLog(module = "标段管理", action = "标记重点", description = "标记/取消标记重点区域, 标段ID: #id, 是否重点: #isKeyArea", riskLevel = 0)
    public Result<Void> toggleKeyArea(@Parameter(description = "标段ID") @PathVariable Long id,
                                        @Parameter(description = "是否重点区域：0否/1是") @RequestParam Integer isKeyArea) {
        projectSectionService.toggleKeyArea(id, isKeyArea);
        return Result.success();
    }

    @Operation(summary = "获取标段审核积压信息", description = "获取标段下待审核结果的积压统计")
    @GetMapping("/{id}/audit-backlog")
    @PreAuthorize("hasAuthority('" + Permissions.PROJECT_VIEW + "')")
    public Result<Map<String, Object>> getAuditBacklog(@Parameter(description = "标段ID") @PathVariable Long id) {
        return Result.success(projectSectionService.getAuditBacklog(id));
    }
}