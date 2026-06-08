package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.entity.LocationCorrectionLog;
import com.qhiot.survey.service.LocationCorrectionService;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 位置纠偏控制器
 */
@Tag(name = "位置纠偏", description = "地图纠偏轨迹管理")
@RestController
@RequestMapping("/api/v1/location-correction")
public class LocationCorrectionController {

    @Autowired
    private LocationCorrectionService locationCorrectionService;

    @Operation(summary = "分页查询纠偏日志", description = "分页查询位置纠偏日志，可按点位ID筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<Page<LocationCorrectionLog>> listByPage(
            @RequestParam(required = false) Long pointId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(locationCorrectionService.listByPage(pointId, pageNum, pageSize));
    }

    @Operation(summary = "获取点位纠偏轨迹", description = "获取指定点位的所有纠偏轨迹记录")
    @GetMapping("/trajectory/{pointId}")
    @PreAuthorize("hasAuthority('" + Permissions.POINT_VIEW + "')")
    public Result<List<LocationCorrectionLog>> getTrajectory(@Parameter(description = "勘察点位ID") @PathVariable Long pointId) {
        return Result.success(locationCorrectionService.getCorrectionTrajectory(pointId));
    }

    @Operation(summary = "保存纠偏日志", description = "保存一条位置纠偏日志记录")
    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.POINT_EDIT + "')")
    @OperationLog(module = "位置纠偏", action = "保存纠偏", description = "保存位置纠偏日志, 点位ID: #log.pointId", riskLevel = 0)
    public Result<Void> saveCorrectionLog(@RequestBody LocationCorrectionLog log) {
        locationCorrectionService.saveCorrectionLog(log);
        return Result.success();
    }
}