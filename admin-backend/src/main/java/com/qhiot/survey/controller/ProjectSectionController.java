package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
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
@RequestMapping("/api/section")
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
    public Result<ProjectSection> create(@RequestBody ProjectSection section) {
        return Result.success(projectSectionService.createSection(section));
    }

    @Operation(summary = "更新标段")
    @PutMapping("/{id}")
    public Result<ProjectSection> update(@PathVariable Long id, @RequestBody ProjectSection section) {
        return Result.success(projectSectionService.updateSection(id, section));
    }

    @Operation(summary = "删除标段")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectSectionService.deleteSection(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用标段")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        projectSectionService.toggleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "设置标段负责人")
    @PutMapping("/{id}/manager")
    public Result<Void> setManager(@PathVariable Long id, @RequestParam Long managerId) {
        projectSectionService.setManager(id, managerId);
        return Result.success();
    }

    @Operation(summary = "绑定点位到标段")
    @PostMapping("/{id}/bind-points")
    public Result<Void> bindPoints(@PathVariable Long id, @RequestBody List<Long> pointIds) {
        projectSectionService.bindPoints(id, pointIds);
        return Result.success();
    }

    @Operation(summary = "解绑点位")
    @PostMapping("/{id}/unbind-points")
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
