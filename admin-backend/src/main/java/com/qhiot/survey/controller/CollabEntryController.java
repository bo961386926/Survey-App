package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.CollabEntry;
import com.qhiot.survey.service.CollabEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 协作入口控制器
 */
@Tag(name = "协作入口", description = "第三方协作入口管理")
@RestController
@RequestMapping("/api/collab")
public class CollabEntryController {

    @Autowired
    private CollabEntryService collabEntryService;

    @Operation(summary = "分页查询协作入口列表")
    @GetMapping("/page")
    public Result<Page<CollabEntry>> listByPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(collabEntryService.listByPage(keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取协作入口详情")
    @GetMapping("/{id}")
    public Result<CollabEntry> getById(@PathVariable Long id) {
        return Result.success(collabEntryService.getById(id));
    }

    @Operation(summary = "创建协作入口")
    @PostMapping
    public Result<CollabEntry> create(@RequestBody CollabEntry entry) {
        return Result.success(collabEntryService.createEntry(entry));
    }

    @Operation(summary = "更新协作入口")
    @PutMapping("/{id}")
    public Result<CollabEntry> update(@PathVariable Long id, @RequestBody CollabEntry entry) {
        return Result.success(collabEntryService.updateEntry(id, entry));
    }

    @Operation(summary = "撤销协作入口")
    @PutMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        collabEntryService.revokeEntry(id);
        return Result.success();
    }

    @Operation(summary = "重置Token")
    @PutMapping("/{id}/reset-token")
    public Result<String> resetToken(@PathVariable Long id) {
        return Result.success(collabEntryService.resetToken(id));
    }

    @Operation(summary = "根据Token获取协作入口")
    @GetMapping("/by-token")
    public Result<CollabEntry> getByToken(@RequestParam String token) {
        return Result.success(collabEntryService.getByToken(token));
    }

    @Operation(summary = "获取访问日志")
    @GetMapping("/{entryId}/logs")
    public Result<List<Object>> getAccessLogs(@PathVariable Long entryId) {
        return Result.success(collabEntryService.getAccessLogs(entryId));
    }
}