package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.ProjectSection;
import com.qhiot.survey.service.ProjectSectionService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "分页查询标段列表")
    @GetMapping("/page")
    public Result<Page<ProjectSection>> listByPage(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(projectSectionService.listByPage(projectId, keyword, pageNum, pageSize));
    }

    @Operation(summary = "根据项目ID获取标段列表")
    @GetMapping("/list")
    public Result<List<ProjectSection>> listByProjectId(@RequestParam Long projectId) {
        return Result.success(projectSectionService.listByProjectId(projectId));
    }

    @Operation(summary = "获取标段详情")
    @GetMapping("/{id}")
    public Result<ProjectSection> getById(@PathVariable Long id) {
        return Result.success(projectSectionService.getById(id));
    }

    @Operation(summary = "创建标段")
    @PostMapping
    @OperationLog(module = "标段管理", action = "创建", description = "创建标段: #section.sectionName", riskLevel = 1)
    public Result<ProjectSection> create(@RequestBody ProjectSection section) {
        return Result.success(projectSectionService.createSection(section));
    }

    @Operation(summary = "更新标段")
    @PutMapping("/{id}")
    @OperationLog(module = "标段管理", action = "更新", description = "更新标段ID: #id", riskLevel = 1)
    public Result<ProjectSection> update(@PathVariable Long id, @RequestBody ProjectSection section) {
        return Result.success(projectSectionService.updateSection(id, section));
    }

    @Operation(summary = "删除标段")
    @DeleteMapping("/{id}")
    @OperationLog(module = "标段管理", action = "删除", description = "删除标段ID: #id", riskLevel = 2)
    public Result<Void> delete(@PathVariable Long id) {
        projectSectionService.deleteSection(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用标段")
    @PutMapping("/{id}/status")
    @OperationLog(module = "标段管理", action = "变更状态", description = "变更标段状态, ID: #id, 目标状态: #status", riskLevel = 1)
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        projectSectionService.toggleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "设置标段负责人")
    @PutMapping("/{id}/manager")
    @OperationLog(module = "标段管理", action = "设置负责人", description = "设置标段负责人, 标段ID: #id, 负责人ID: #managerId", riskLevel = 1)
    public Result<Void> setManager(@PathVariable Long id, @RequestParam Long managerId) {
        projectSectionService.setManager(id, managerId);
        return Result.success();
    }

    @Operation(summary = "绑定点位到标段")
    @PostMapping("/{id}/bind-points")
    @OperationLog(module = "标段管理", action = "绑定点位", description = "绑定点位到标段, 标段ID: #id, 点位数量: #pointIds.size()", riskLevel = 0)
    public Result<Void> bindPoints(@PathVariable Long id, @RequestBody List<Long> pointIds) {
        projectSectionService.bindPoints(id, pointIds);
        return Result.success();
    }

    @Operation(summary = "解绑点位")
    @PostMapping("/{id}/unbind-points")
    @OperationLog(module = "标段管理", action = "解绑点位", description = "解绑标段点位, 标段ID: #id, 点位数量: #pointIds.size()", riskLevel = 0)
    public Result<Void> unbindPoints(@PathVariable Long id, @RequestBody List<Long> pointIds) {
        projectSectionService.unbindPoints(id, pointIds);
        return Result.success();
    }

    @Operation(summary = "获取标段统计信息")
    @GetMapping("/{id}/statistics")
    public Result<Object> getStatistics(@PathVariable Long id) {
        return Result.success(projectSectionService.getStatistics(id));
    }

    @Operation(summary = "标记/取消标记重点区域")
    @PutMapping("/{id}/key-area")
    @OperationLog(module = "标段管理", action = "标记重点", description = "标记/取消标记重点区域, 标段ID: #id, 是否重点: #isKeyArea", riskLevel = 0)
    public Result<Void> toggleKeyArea(@PathVariable Long id, @RequestParam Integer isKeyArea) {
        projectSectionService.toggleKeyArea(id, isKeyArea);
        return Result.success();
    }

    @Operation(summary = "获取标段审核积压信息")
    @GetMapping("/{id}/audit-backlog")
    public Result<Map<String, Object>> getAuditBacklog(@PathVariable Long id) {
        return Result.success(projectSectionService.getAuditBacklog(id));
    }
}
