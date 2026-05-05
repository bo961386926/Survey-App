package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.service.SurveyResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 勘查结果管理控制器
 */
@Tag(name = "勘查结果管理", description = "勘查结果采集、审核相关接口")
@RestController
@RequestMapping("/api/v1/result")
@RequiredArgsConstructor
public class SurveyResultController {

    private final SurveyResultService surveyResultService;

    @Operation(summary = "获取勘查结果列表")
    @GetMapping("/list")
    public Result<List<SurveyResult>> getResultList(@RequestParam(required = false) Long pointId) {
        List<SurveyResult> results;
        if (pointId != null) {
            results = surveyResultService.getResultsByPointId(pointId);
        } else {
            results = surveyResultService.list();
        }
        return Result.success(results);
    }

    @Operation(summary = "获取勘查结果详情")
    @GetMapping("/{id}")
    public Result<SurveyResult> getResultById(@PathVariable Long id) {
        SurveyResult result = surveyResultService.getById(id);
        return result != null ? Result.success(result) : Result.error("结果不存在");
    }

    @Operation(summary = "获取点位最新勘查结果")
    @GetMapping("/point/{pointId}/latest")
    public Result<SurveyResult> getLatestResult(@PathVariable Long pointId) {
        SurveyResult result = surveyResultService.getLatestResultByPointId(pointId);
        return result != null ? Result.success(result) : Result.error("暂无勘查结果");
    }

    @Operation(summary = "创建勘查结果")
    @PostMapping("/create")
    @PreAuthorize("hasRole('COLLECTOR')")
    public Result<SurveyResult> createResult(@RequestBody SurveyResult result) {
        return Result.success(surveyResultService.createResult(result));
    }

    @Operation(summary = "更新勘查结果")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('COLLECTOR')")
    public Result<SurveyResult> updateResult(@PathVariable Long id,
                                              @RequestBody SurveyResult result,
                                              @RequestParam(required = false) Integer expectedVersion) {
        return Result.success(surveyResultService.updateResult(id, result, expectedVersion));
    }

    @Operation(summary = "删除勘查结果")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteResult(@PathVariable Long id) {
        boolean success = surveyResultService.deleteResult(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "分页查询审核列表")
    @GetMapping("/audit/page")
    @PreAuthorize("hasRole('AUDITOR')")
    public Result<PageResult<SurveyResult>> queryAuditPage(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(surveyResultService.queryAuditPage(projectId, sectionId, status, pageNum, pageSize));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/audit/{id}/pass")
    @PreAuthorize("hasRole('AUDITOR')")
    public Result<Boolean> passAudit(@PathVariable Long id,
                                      @RequestParam(required = false) String auditRemark) {
        // 获取当前审核员ID（从SecurityContext）
        Long auditorId = getCurrentUserId();
        boolean success = surveyResultService.passAudit(id, auditRemark, auditorId);
        return success ? Result.success(true) : Result.error("审核失败");
    }

    @Operation(summary = "审核驳回")
    @PostMapping("/audit/{id}/reject")
    @PreAuthorize("hasRole('AUDITOR')")
    public Result<Boolean> rejectAudit(@PathVariable Long id,
                                        @RequestParam String auditRemark) {
        // 获取当前审核员ID（从SecurityContext）
        Long auditorId = getCurrentUserId();
        boolean success = surveyResultService.rejectAudit(id, auditRemark, auditorId);
        return success ? Result.success(true) : Result.error("驳回失败");
    }

    @Operation(summary = "批量审核通过")
    @PostMapping("/audit/batch-pass")
    @PreAuthorize("hasRole('AUDITOR')")
    public Result<Boolean> batchPassAudit(@RequestBody List<Long> ids,
                                           @RequestParam(required = false) String auditRemark) {
        // 获取当前审核员ID（从SecurityContext）
        Long auditorId = getCurrentUserId();
        boolean success = surveyResultService.batchPassAudit(ids, auditRemark, auditorId);
        return success ? Result.success(true) : Result.error("批量审核失败");
    }

    @Operation(summary = "提交审核")
    @PostMapping("/submit/{id}")
    @PreAuthorize("hasRole('COLLECTOR')")
    public Result<Boolean> submitForAudit(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        boolean success = surveyResultService.submitForAudit(id, userId);
        return success ? Result.success(true) : Result.error("提交失败");
    }

    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    @PreAuthorize("hasRole('COLLECTOR')")
    public Result<SurveyResult> saveDraft(@RequestBody SurveyResult result) {
        Long userId = getCurrentUserId();
        return Result.success(surveyResultService.saveDraft(result, userId));
    }

    @Operation(summary = "获取版本差异对比")
    @GetMapping("/version/diff")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'PROJECT_LEADER')")
    public Result<Map<String, Object>> getVersionDiff(@RequestParam Long currentId,
                                                       @RequestParam Long compareId) {
        return Result.success(surveyResultService.getVersionDiff(currentId, compareId));
    }

    @Operation(summary = "获取用户勘查结果")
    @GetMapping("/user/{surveyUserId}")
    public Result<List<SurveyResult>> getResultsByUser(@PathVariable Long surveyUserId) {
        List<SurveyResult> results = surveyResultService.getResultsByUser(surveyUserId);
        return Result.success(results);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext中获取当前用户ID
        return 1L;
    }
}