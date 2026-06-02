package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.CollabEntry;
import com.qhiot.survey.service.CollabEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 协作入口控制器
 */
@Tag(name = "协作入口", description = "第三方协作入口管理")
@RestController
@RequestMapping("/api/v1/collab")
public class CollabEntryController {

    @Autowired
    private CollabEntryService collabEntryService;

    @Operation(summary = "分页查询协作入口列表", description = "支持按关键词模糊搜索，分页返回协作入口列表")
    @GetMapping("/page")
    public Result<Page<CollabEntry>> listByPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(collabEntryService.listByPage(keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取协作入口详情", description = "根据ID获取协作入口详细信息")
    @GetMapping("/{id}")
    public Result<CollabEntry> getById(@Parameter(description = "协作入口ID") @PathVariable Long id) {
        return Result.success(collabEntryService.getById(id));
    }

    @Operation(summary = "创建协作入口", description = "创建新的第三方协作入口配置")
    @PostMapping
    @OperationLog(module = "协作入口", action = "创建", description = "创建协作入口: #entry.entryName", riskLevel = 1)
    public Result<CollabEntry> create(@RequestBody CollabEntry entry) {
        return Result.success(collabEntryService.createEntry(entry));
    }

    @Operation(summary = "更新协作入口", description = "更新协作入口配置信息")
    @PutMapping("/{id}")
    @OperationLog(module = "协作入口", action = "更新", description = "更新协作入口ID: #id", riskLevel = 1)
    public Result<CollabEntry> update(@Parameter(description = "协作入口ID") @PathVariable Long id, @RequestBody CollabEntry entry) {
        return Result.success(collabEntryService.updateEntry(id, entry));
    }

    @Operation(summary = "撤销协作入口", description = "撤销协作入口使其失效，已签发的Token仍可用至过期")
    @PutMapping("/{id}/revoke")
    @OperationLog(module = "协作入口", action = "撤销", description = "撤销协作入口ID: #id", riskLevel = 2)
    public Result<Void> revoke(@Parameter(description = "协作入口ID") @PathVariable Long id) {
        collabEntryService.revokeEntry(id);
        return Result.success();
    }

    @Operation(summary = "重置Token", description = "重新生成协作入口的访问Token，旧Token立即失效")
    @PutMapping("/{id}/reset-token")
    @OperationLog(module = "协作入口", action = "重置Token", description = "重置协作入口Token, ID: #id", riskLevel = 2)
    public Result<String> resetToken(@Parameter(description = "协作入口ID") @PathVariable Long id) {
        return Result.success(collabEntryService.resetToken(id));
    }

    @Operation(summary = "根据Token获取协作入口", description = "通过协作Token查询对应的协作入口配置")
    @GetMapping("/by-token")
    public Result<CollabEntry> getByToken(@Parameter(description = "协作访问Token") @RequestParam String token) {
        return Result.success(collabEntryService.getByToken(token));
    }

    @Operation(summary = "获取访问日志", description = "获取指定协作入口的访问日志列表")
    @GetMapping("/{entryId}/logs")
    public Result<List<Object>> getAccessLogs(@Parameter(description = "协作入口ID") @PathVariable Long entryId) {
        return Result.success(collabEntryService.getAccessLogs(entryId));
    }

    @Operation(summary = "签发协作访问 JWT", description = "为指定协作入口签发JWT令牌（loginType=collab），用于第三方系统免登录访问")
    @PostMapping("/{id}/issue-token")
    @OperationLog(module = "协作入口", action = "签发Token", description = "签发协作JWT, 入口ID: #id", riskLevel = 2)
    public Result<String> issueToken(@Parameter(description = "协作入口ID") @PathVariable Long id) {
        return Result.success(collabEntryService.issueCollabToken(id));
    }
}