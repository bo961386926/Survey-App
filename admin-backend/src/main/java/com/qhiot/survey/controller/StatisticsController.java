package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计概览控制器
 */
@Tag(name = "统计概览", description = "系统统计与概览数据接口")
@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "获取系统概览统计", description = "获取系统总用户数、项目数、点位数、审核率等全局统计数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getSystemOverview() {
        Map<String, Object> overview = statisticsService.getSystemOverview();
        return Result.success(overview);
    }

    @Operation(summary = "获取用户统计", description = "获取用户活跃度、角色分布等用户维度的统计数据")
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = statisticsService.getUserStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "获取项目统计", description = "获取项目进度、状态分布等项目维度的统计数据")
    @GetMapping("/projects")
    public Result<Map<String, Object>> getProjectStatistics() {
        Map<String, Object> stats = statisticsService.getProjectStatistics();
        return Result.success(stats);
    }
}