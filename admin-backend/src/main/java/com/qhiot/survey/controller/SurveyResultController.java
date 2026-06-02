package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SurveyResultService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SysUserService sysUserService;

    @Operation(summary = "获取勘查结果列表", description = "查询所有勘查结果，可按点位ID筛选")
    @GetMapping("/list")
    public Result<List<SurveyResult>> getResultList(@Parameter(description = "勘察点位ID") @RequestParam(required = false) Long pointId) {
        List<SurveyResult> results;
        if (pointId != null) {
            results = surveyResultService.getResultsByPointId(pointId);
        } else {
            results = surveyResultService.list();
        }
        return Result.success(results);
    }

    @Operation(summary = "获取勘查结果详情", description = "根据结果ID获取勘查结果详细信息")
    @GetMapping("/{id}")
    public Result<SurveyResult> getResultById(@Parameter(description = "勘查结果ID") @PathVariable Long id) {
        SurveyResult result = surveyResultService.getById(id);
        return result != null ? Result.success(result) : Result.error("结果不存在");
    }

    @Operation(summary = "获取点位最新勘查结果", description = "获取指定点位的最新一条勘查结果")
    @GetMapping("/point/{pointId}/latest")
    public Result<SurveyResult> getLatestResult(@Parameter(description = "勘察点位ID") @PathVariable Long pointId) {
        SurveyResult result = surveyResultService.getLatestResultByPointId(pointId);
        return result != null ? Result.success(result) : Result.error("暂无勘查结果");
    }

    @Operation(summary = "创建勘查结果", description = "采集人员创建新的勘查结果，需COLLECTOR角色")
    @PostMapping("/create")
    @PreAuthorize("hasRole('COLLECTOR')")
    @OperationLog(module = "勘查管理", action = "创建", description = "创建勘查结果", riskLevel = 1)
    public Result<SurveyResult> createResult(@RequestBody SurveyResult result) {
        return Result.success(surveyResultService.createResult(result));
    }

    @Operation(summary = "更新勘查结果", description = "更新勘查结果数据，支持乐观锁版本号检测并发冲突")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('COLLECTOR')")
    @OperationLog(module = "勘查管理", action = "更新", description = "更新勘查结果, ID: #id", riskLevel = 1)
    public Result<SurveyResult> updateResult(@Parameter(description = "勘查结果ID") @PathVariable Long id,
                                              @RequestBody SurveyResult result,
                                              @Parameter(description = "期望版本号（乐观锁）") @RequestParam(required = false) Integer expectedVersion) {
        return Result.success(surveyResultService.updateResult(id, result, expectedVersion));
    }

    @Operation(summary = "删除勘查结果", description = "删除勘查结果，需ADMIN角色")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "勘查管理", action = "删除", description = "删除勘查结果, ID: #id", riskLevel = 2)
    public Result<Boolean> deleteResult(@Parameter(description = "勘查结果ID") @PathVariable Long id) {
        boolean success = surveyResultService.deleteResult(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "分页查询审核列表", description = "审核员分页查询待审核勘查结果，支持按项目、标段、状态筛选")
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

    @Operation(summary = "审核通过", description = "审核员通过勘查结果审核")
    @PostMapping("/audit/{id}/pass")
    @PreAuthorize("hasRole('AUDITOR')")
    @OperationLog(module = "审核管理", action = "审核通过", description = "审核通过, 结果ID: #id", riskLevel = 1)
    public Result<Boolean> passAudit(@Parameter(description = "勘查结果ID") @PathVariable Long id,
                                      @Parameter(description = "审核备注") @RequestParam(required = false) String auditRemark) {
        // 获取当前审核员ID（从SecurityContext）
        Long auditorId = getCurrentUserId();
        boolean success = surveyResultService.passAudit(id, auditRemark, auditorId);
        return success ? Result.success(true) : Result.error("审核失败");
    }

    @Operation(summary = "审核驳回", description = "审核员驳回勘查结果，必须填写驳回理由")
    @PostMapping("/audit/{id}/reject")
    @PreAuthorize("hasRole('AUDITOR')")
    @OperationLog(module = "审核管理", action = "审核驳回", description = "审核驳回, 结果ID: #id", riskLevel = 1)
    public Result<Boolean> rejectAudit(@Parameter(description = "勘查结果ID") @PathVariable Long id,
                                        @Parameter(description = "审核备注（必填）") @RequestParam String auditRemark) {
        // 获取当前审核员ID（从SecurityContext）
        Long auditorId = getCurrentUserId();
        boolean success = surveyResultService.rejectAudit(id, auditRemark, auditorId);
        return success ? Result.success(true) : Result.error("驳回失败");
    }

    @Operation(summary = "批量审核通过", description = "批量通过多条勘查结果审核")
    @PostMapping("/audit/batch-pass")
    @PreAuthorize("hasRole('AUDITOR')")
    @OperationLog(module = "审核管理", action = "批量审核通过", description = "批量审核通过, 数量: #ids.size()", riskLevel = 1)
    public Result<Boolean> batchPassAudit(@RequestBody List<Long> ids,
                                           @RequestParam(required = false) String auditRemark) {
        // 获取当前审核员ID（从SecurityContext）
        Long auditorId = getCurrentUserId();
        boolean success = surveyResultService.batchPassAudit(ids, auditRemark, auditorId);
        return success ? Result.success(true) : Result.error("批量审核失败");
    }

    @Operation(summary = "提交审核", description = "采集人员提交勘查结果进行审核，支持versionNo乐观锁冲突检测")
    @PostMapping("/submit/{id}")
    @PreAuthorize("hasRole('COLLECTOR')")
    @OperationLog(module = "勘查管理", action = "提交审核", description = "提交审核, 结果ID: #id", riskLevel = 0)
    public Result<Boolean> submitForAudit(@Parameter(description = "勘查结果ID") @PathVariable Long id,
                                           @Parameter(description = "客户端所持版本号，用于并发冲突检测") @RequestParam(required = false) Integer versionNo) {
        Long userId = getCurrentUserId();
        // 传入客户端所持有的 versionNo 进行并发冲突检测，冲突时会抛出 409 VERSION_CONFLICT 由全局异常处理器返回
        boolean success = surveyResultService.submitForAudit(id, userId, versionNo);
        return success ? Result.success(true) : Result.error("提交失败");
    }

    @Operation(summary = "保存草稿", description = "采集人员保存勘查结果草稿")
    @PostMapping("/draft")
    @PreAuthorize("hasRole('COLLECTOR')")
    @OperationLog(module = "勘查管理", action = "保存草稿", description = "保存勘查草稿", riskLevel = 0)
    public Result<SurveyResult> saveDraft(@RequestBody SurveyResult result) {
        Long userId = getCurrentUserId();
        return Result.success(surveyResultService.saveDraft(result, userId));
    }

    @Operation(summary = "获取版本差异对比", description = "对比两个版本的勘查结果数据差异，需ADMIN/AUDITOR/PROJECT_LEADER角色")
    @GetMapping("/version/diff")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'PROJECT_LEADER')")
    public Result<Map<String, Object>> getVersionDiff(@RequestParam Long currentId,
                                                       @RequestParam Long compareId) {
        return Result.success(surveyResultService.getVersionDiff(currentId, compareId));
    }

    @Operation(summary = "获取用户勘查结果", description = "查询指定采集人员的所有勘查结果")
    @GetMapping("/user/{surveyUserId}")
    public Result<List<SurveyResult>> getResultsByUser(@Parameter(description = "采集人员用户ID") @PathVariable Long surveyUserId) {
        List<SurveyResult> results = surveyResultService.getResultsByUser(surveyUserId);
        return Result.success(results);
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