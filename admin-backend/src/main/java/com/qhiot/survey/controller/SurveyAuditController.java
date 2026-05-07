package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SurveyAuditService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "分页查询待审核列表")
    @GetMapping("/pending")
    public Result<Page<SurveyResult>> getPendingList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long auditorId = getCurrentUserId();
        return Result.success(surveyAuditService.getPendingAuditList(auditorId, keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取审核详情")
    @GetMapping("/detail/{resultId}")
    public Result<SurveyResult> getDetail(@PathVariable Long resultId) {
        return Result.success(surveyAuditService.getAuditDetail(resultId));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/pass")
    @OperationLog(module = "审核管理", action = "审核通过", description = "审核通过, 结果ID: #resultId", riskLevel = 1)
    public Result<Void> pass(@RequestParam Long resultId, @RequestParam String comment) {
        Long auditorId = getCurrentUserId();
        surveyAuditService.passAudit(resultId, auditorId, comment);
        return Result.success();
    }

    @Operation(summary = "审核驳回")
    @PostMapping("/reject")
    @OperationLog(module = "审核管理", action = "审核驳回", description = "审核驳回, 结果ID: #resultId", riskLevel = 1)
    public Result<Void> reject(@RequestParam Long resultId,
                               @RequestParam String comment,
                               @RequestParam(required = false) Long rejectTemplateId) {
        Long auditorId = getCurrentUserId();
        surveyAuditService.rejectAudit(resultId, auditorId, comment, rejectTemplateId);
        return Result.success();
    }

    @Operation(summary = "批量审核通过")
    @PostMapping("/batch-pass")
    @OperationLog(module = "审核管理", action = "批量审核通过", description = "批量审核通过, 数量: #resultIds.size()", riskLevel = 1)
    public Result<Void> batchPass(@RequestBody List<Long> resultIds, @RequestParam(required = false) String comment) {
        Long auditorId = getCurrentUserId();
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

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user.getId();
    }
}