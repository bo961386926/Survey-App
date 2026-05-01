package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getSystemOverview() {
        Map<String, Object> overview = statisticsService.getSystemOverview();
        return Result.success(overview);
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = statisticsService.getUserStatistics();
        return Result.success(stats);
    }

    @GetMapping("/projects")
    public Result<Map<String, Object>> getProjectStatistics() {
        Map<String, Object> stats = statisticsService.getProjectStatistics();
        return Result.success(stats);
    }
}