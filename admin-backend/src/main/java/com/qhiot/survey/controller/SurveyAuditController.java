package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.service.SurveyAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核中心控制器
 */
@Tag(name = "审核中心", description = "审核相关接口")
@RestController
@RequestMapping("/api/v1/audit")
public class SurveyAuditController {

    @Autowired
    private SurveyAuditService surveyAuditService;

    @Operation(summary = "分页查询待审核列表")
    @GetMapping("/pending")
    public Result<Page<SurveyResult>> getPendingList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 实际应该从SecurityContext获取当前用户ID
        Long auditorId = 1L;
        return Result.success(surveyAuditService.getPendingAuditList(auditorId, keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取审核详情")
    @GetMapping("/detail/{resultId}")
    public Result<SurveyResult> getDetail(@PathVariable Long resultId) {
        return Result.success(surveyAuditService.getAuditDetail(resultId));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/pass")
    public Result<Void> pass(@RequestParam Long resultId, @RequestParam String comment) {
        // 实际应该从SecurityContext获取当前用户ID
        Long auditorId = 1L;
        surveyAuditService.passAudit(resultId, auditorId, comment);
        return Result.success();
    }

    @Operation(summary = "审核驳回")
    @PostMapping("/reject")
    public Result<Void> reject(@RequestParam Long resultId,
                               @RequestParam String comment,
                               @RequestParam(required = false) Long rejectTemplateId) {
        // 实际应该从SecurityContext获取当前用户ID
        Long auditorId = 1L;
        surveyAuditService.rejectAudit(resultId, auditorId, comment, rejectTemplateId);
        return Result.success();
    }

    @Operation(summary = "批量审核通过")
    @PostMapping("/batch-pass")
    public Result<Void> batchPass(@RequestBody List<Long> resultIds, @RequestParam(required = false) String comment) {
        // 实际应该从SecurityContext获取当前用户ID
        Long auditorId = 1L;
        surveyAuditService.batchPass(resultIds, auditorId, comment);
        return Result.success();
    }

    @Operation(summary = "获取审核记录")
    @GetMapping("/records")
    public Result<List<SurveyAuditRecord>> getRecords(@RequestParam Long pointId) {
        return Result.success(surveyAuditService.getAuditRecords(pointId));
    }

    @Operation(summary = "获取版本差异")
    @GetMapping("/version-diff")
    public Result<Object> getVersionDiff(@RequestParam Long pointId,
                                         @RequestParam Long currentVersionId,
                                         @RequestParam Long compareVersionId) {
        return Result.success(surveyAuditService.getVersionDiff(pointId, currentVersionId, compareVersionId));
    }
}