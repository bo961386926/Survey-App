package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.entity.LocationCorrectionLog;
import com.qhiot.survey.service.LocationCorrectionService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "分页查询纠偏日志")
    @GetMapping("/page")
    public Result<Page<LocationCorrectionLog>> listByPage(
            @RequestParam(required = false) Long pointId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(locationCorrectionService.listByPage(pointId, pageNum, pageSize));
    }

    @Operation(summary = "获取点位纠偏轨迹")
    @GetMapping("/trajectory/{pointId}")
    public Result<List<LocationCorrectionLog>> getTrajectory(@PathVariable Long pointId) {
        return Result.success(locationCorrectionService.getCorrectionTrajectory(pointId));
    }

    @Operation(summary = "保存纠偏日志")
    @PostMapping
    public Result<Void> saveCorrectionLog(@RequestBody LocationCorrectionLog log) {
        locationCorrectionService.saveCorrectionLog(log);
        return Result.success();
    }
}