package com.survey.controller;

import com.survey.common.Result;
import com.survey.entity.SurveyResult;
import com.survey.service.SurveyResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/result")
@CrossOrigin(origins = "*")
public class SurveyResultController {

    @Autowired
    private SurveyResultService surveyResultService;

    @GetMapping("/list")
    public Result<List<SurveyResult>> getResultList(@RequestParam(required = false) Long pointId) {
        List<SurveyResult> results = surveyResultService.getResultList(pointId);
        return Result.success(results);
    }

    @GetMapping("/latest/{pointId}")
    public Result<SurveyResult> getLatestResult(@PathVariable Long pointId) {
        SurveyResult result = surveyResultService.getLatestResult(pointId);
        return Result.success(result);
    }

    @PostMapping("/submit")
    public Result<Boolean> submitResult(@RequestBody SurveyResult result) {
        boolean success = surveyResultService.submitResult(result);
        return success ? Result.success(true) : Result.error("提交失败");
    }

    @PostMapping("/audit/{id}")
    public Result<Boolean> auditResult(@PathVariable Long id,
                                       @RequestParam Integer auditStatus,
                                       @RequestParam(required = false) String auditRemark) {
        boolean success = surveyResultService.auditResult(id, auditStatus, auditRemark);
        return success ? Result.success(true) : Result.error("审核失败");
    }

    @GetMapping("/audit/list")
    public Result<Map<String, Object>> getAuditList(@RequestParam(required = false) Integer auditStatus) {
        Map<String, Object> data = surveyResultService.getAuditList(auditStatus);
        return Result.success(data);
    }
}