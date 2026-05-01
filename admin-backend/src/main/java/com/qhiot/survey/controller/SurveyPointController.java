package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.service.SurveyPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 点位管理控制器
 */
@Tag(name = "点位管理", description = "勘查点位管理相关接口")
@RestController
@RequestMapping("/api/point")
public class SurveyPointController {

    @Autowired
    private SurveyPointService surveyPointService;

    @Operation(summary = "分页查询点位列表")
    @GetMapping("/page")
    public Result<Page<SurveyPoint>> listByPage(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(surveyPointService.listByPage(projectId, sectionId, keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "获取点位列表")
    @GetMapping("/list")
    public Result<List<SurveyPoint>> getPointList(@RequestParam(required = false) Long projectId) {
        List<SurveyPoint> points;
        if (projectId != null) {
            points = surveyPointService.getPointsByProjectId(projectId);
        } else {
            points = surveyPointService.list();
        }
        return Result.success(points);
    }

    @Operation(summary = "获取点位详情")
    @GetMapping("/{id}")
    public Result<SurveyPoint> getPointById(@PathVariable Long id) {
        SurveyPoint point = surveyPointService.getById(id);
        return point != null ? Result.success(point) : Result.error("点位不存在");
    }

    @Operation(summary = "创建点位")
    @PostMapping("/create")
    public Result<SurveyPoint> createPoint(@RequestBody SurveyPoint point) {
        return Result.success(surveyPointService.createPoint(point));
    }

    @Operation(summary = "更新点位")
    @PutMapping("/update/{id}")
    public Result<SurveyPoint> updatePoint(@PathVariable Long id, @RequestBody SurveyPoint point) {
        return Result.success(surveyPointService.updatePoint(id, point));
    }

    @Operation(summary = "删除点位")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deletePoint(@PathVariable Long id) {
        surveyPointService.deletePoint(id);
        return Result.success();
    }

    @Operation(summary = "批量创建点位")
    @PostMapping("/batch")
    public Result<Boolean> batchCreatePoints(@RequestBody List<SurveyPoint> points) {
        boolean success = surveyPointService.batchCreatePoints(points);
        return success ? Result.success(true) : Result.error("批量创建失败");
    }

    @Operation(summary = "根据状态获取点位列表")
    @GetMapping("/status/{status}")
    public Result<List<SurveyPoint>> getPointsByStatus(@PathVariable Integer status) {
        List<SurveyPoint> points = surveyPointService.getPointsByStatus(status);
        return Result.success(points);
    }

    @Operation(summary = "Excel导入点位")
    @PostMapping("/import")
    public Result<Map<String, Object>> importFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long projectId) {
        return Result.success(surveyPointService.importFromExcel(file, projectId));
    }

    @Operation(summary = "批量分配点位")
    @PostMapping("/batch-assign")
    public Result<Void> batchAssign(
            @RequestParam Long projectId,
            @RequestBody List<Long> pointIds,
            @RequestParam Long assigneeId) {
        surveyPointService.batchAssign(projectId, pointIds, assigneeId);
        return Result.success();
    }

    @Operation(summary = "点位作废")
    @PostMapping("/{id}/invalidate")
    public Result<Void> invalidatePoint(@PathVariable Long id, @RequestParam String reason) {
        surveyPointService.invalidatePoint(id, reason);
        return Result.success();
    }

    @Operation(summary = "获取点位历史版本")
    @GetMapping("/{id}/history")
    public Result<List<Map<String, Object>>> getPointHistory(@PathVariable Long id) {
        return Result.success(surveyPointService.getPointHistory(id));
    }

    @Operation(summary = "批量设置排口类型")
    @PostMapping("/batch-set-outfall-type")
    public Result<Void> batchSetOutfallType(
            @RequestBody List<Long> pointIds,
            @RequestParam String outfallType) {
        surveyPointService.batchSetOutfallType(pointIds, outfallType);
        return Result.success();
    }
}