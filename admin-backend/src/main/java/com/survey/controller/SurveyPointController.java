package com.survey.controller;

import com.survey.common.Result;
import com.survey.entity.SurveyPoint;
import com.survey.service.SurveyPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/point")
@CrossOrigin(origins = "*")
public class SurveyPointController {

    @Autowired
    private SurveyPointService surveyPointService;

    @GetMapping("/list")
    public Result<List<SurveyPoint>> getPointList(@RequestParam(required = false) Long projectId) {
        List<SurveyPoint> points = surveyPointService.getPointList(projectId);
        return Result.success(points);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getPointDetail(@PathVariable Long id) {
        Map<String, Object> detail = surveyPointService.getPointDetail(id);
        return detail != null ? Result.success(detail) : Result.error("点位不存在");
    }

    @PostMapping("/create")
    public Result<Boolean> createPoint(@RequestBody SurveyPoint point) {
        boolean success = surveyPointService.createPoint(point);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @PutMapping("/update")
    public Result<Boolean> updatePoint(@RequestBody SurveyPoint point) {
        boolean success = surveyPointService.updatePoint(point);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deletePoint(@PathVariable Long id) {
        boolean success = surveyPointService.deletePoint(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }
}