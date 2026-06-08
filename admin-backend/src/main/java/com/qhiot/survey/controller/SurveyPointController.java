package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.dto.SurveyPointDTO;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.service.SurveyPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 点位管理控制器
 */
@Tag(name = "点位管理", description = "勘查点位管理相关接口")
@RestController
@RequestMapping("/api/v1/point")
public class SurveyPointController {

    @Autowired
    private SurveyPointService surveyPointService;

    @Operation(summary = "分页查询点位列表", description = "分页查询勘察点位，支持按项目、标段、关键词、状态筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<Page<SurveyPointDTO>> listByPage(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(surveyPointService.listByPageWithProject(projectId, sectionId, keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "获取点位列表", description = "获取点位简要列表，可按项目ID筛选")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<List<SurveyPoint>> getPointList(@RequestParam(required = false) Long projectId) {
        List<SurveyPoint> points;
        if (projectId != null) {
            points = surveyPointService.getPointsByProjectId(projectId);
        } else {
            points = surveyPointService.getAccessiblePoints(null);
        }
        return Result.success(points);
    }

    @Operation(summary = "获取点位详情", description = "根据点位ID获取详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<SurveyPoint> getPointById(@Parameter(description = "点位ID") @PathVariable Long id) {
        SurveyPoint point = surveyPointService.getAccessiblePointById(id);
        return point != null ? Result.success(point) : Result.error("点位不存在");
    }

    @Operation(summary = "创建点位", description = "创建单个勘察点位")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "创建", description = "创建点位: #point.pointName", riskLevel = 0)
    public Result<SurveyPoint> createPoint(@RequestBody SurveyPoint point) {
        return Result.success(surveyPointService.createPoint(point));
    }

    @Operation(summary = "更新点位", description = "更新点位信息")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "更新", description = "更新点位ID: #id", riskLevel = 0)
    public Result<SurveyPoint> updatePoint(@Parameter(description = "点位ID") @PathVariable Long id, @RequestBody SurveyPoint point) {
        return Result.success(surveyPointService.updatePoint(id, point));
    }

    @Operation(summary = "删除点位", description = "删除点位，高风险操作")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "删除", description = "删除点位ID: #id", riskLevel = 2)
    public Result<Void> deletePoint(@Parameter(description = "点位ID") @PathVariable Long id) {
        surveyPointService.deletePoint(id);
        return Result.success();
    }

    @Operation(summary = "批量创建点位", description = "批量创建多个勘察点位")
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "批量创建", description = "批量创建点位, 数量: #points.size()", riskLevel = 0)
    public Result<Boolean> batchCreatePoints(@RequestBody List<SurveyPoint> points) {
        boolean success = surveyPointService.batchCreatePoints(points);
        return success ? Result.success(true) : Result.error("批量创建失败");
    }

    @Operation(summary = "根据状态获取点位列表", description = "按点位状态筛选点位列表")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<List<SurveyPoint>> getPointsByStatus(@Parameter(description = "点位状态") @PathVariable Integer status) {
        List<SurveyPoint> points = surveyPointService.getPointsByStatus(status);
        return Result.success(points);
    }

    @Operation(summary = "Excel导入点位", description = "从Excel文件批量导入勘察点位到指定项目")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "导入", description = "Excel导入点位, 项目ID: #projectId", riskLevel = 1)
    public Result<Map<String, Object>> importFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long projectId) {
        return Result.success(surveyPointService.importFromExcel(file, projectId));
    }

    @Operation(summary = "批量分配点位", description = "将多个点位批量分配给指定采集人员")
    @PostMapping("/batch-assign")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "批量分配", description = "批量分配点位, 项目ID: #projectId, 点位数量: #pointIds.size(), 分配给用户ID: #assigneeId", riskLevel = 1)
    public Result<Void> batchAssign(
            @RequestParam Long projectId,
            @RequestBody List<Long> pointIds,
            @RequestParam Long assigneeId) {
        surveyPointService.batchAssign(projectId, pointIds, assigneeId);
        return Result.success();
    }

    @Operation(summary = "点位作废", description = "将点位标记为作废状态，需提供作废原因")
    @PostMapping("/{id}/invalidate")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "作废", description = "点位作废, 点位ID: #id, 原因: #reason", riskLevel = 1)
    public Result<Void> invalidatePoint(@Parameter(description = "点位ID") @PathVariable Long id,
                                          @Parameter(description = "作废原因") @RequestParam String reason) {
        surveyPointService.invalidatePoint(id, reason);
        return Result.success();
    }

    @Operation(summary = "获取点位历史版本", description = "获取点位所有历史版本记录")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<List<Map<String, Object>>> getPointHistory(@Parameter(description = "点位ID") @PathVariable Long id) {
        return Result.success(surveyPointService.getPointHistory(id));
    }

    @Operation(summary = "批量设置排口类型", description = "批量设置多个点位的排口类型")
    @PostMapping("/batch-set-outfall-type")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "点位管理", action = "批量设置排口", description = "批量设置排口类型, 点位数量: #pointIds.size(), 排口类型: #outfallType", riskLevel = 0)
    public Result<Void> batchSetOutfallType(
            @RequestBody List<Long> pointIds,
            @Parameter(description = "排口类型") @RequestParam String outfallType) {
        surveyPointService.batchSetOutfallType(pointIds, outfallType);
        return Result.success();
    }

    // =========== 地图相关 API ===========

    @Operation(summary = "获取地图点位数据", description = "获取地图图层渲染所需的点位数据（含经纬度+状态），支持项目/标段/状态筛选")
    @GetMapping("/map")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<List<SurveyPointDTO>> getMapPoints(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "标段ID") @RequestParam(required = false) Long sectionId,
            @Parameter(description = "点位状态") @RequestParam(required = false) Integer status) {
        return Result.success(surveyPointService.getMapPoints(projectId, sectionId, status));
    }

    @Operation(summary = "获取点位地图统计", description = "按状态聚合统计点位数量，用于地图概览")
    @GetMapping("/map/statistics")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<Map<String, Long>> getMapPointStatistics(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId) {
        return Result.success(surveyPointService.getMapPointStatistics(projectId));
    }

    @Operation(summary = "获取异常点位列表", description = "获取带异常标签的点位，用于地图异常点位高亮")
    @GetMapping("/map/abnormal")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<List<SurveyPointDTO>> getAbnormalPoints(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId) {
        return Result.success(surveyPointService.getAbnormalPoints(projectId));
    }
}
