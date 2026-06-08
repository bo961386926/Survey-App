package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.entity.Announcement;
import com.qhiot.survey.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 公告中心控制器
 */
@Tag(name = "公告中心", description = "公告管理相关接口")
@RestController
@RequestMapping("/api/v1/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Operation(summary = "分页查询公告列表", description = "支持按标题关键词、类型、状态筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_ANNOUNCEMENT + "')")
    public Result<Page<Announcement>> listByPage(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "公告类型") @RequestParam(required = false) String type,
            @Parameter(description = "状态：0草稿/1定时发布/2已发布/3已撤回") @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(announcementService.listByPage(keyword, type, status, pageNum, pageSize));
    }

    @Operation(summary = "获取公告详情", description = "根据ID获取公告详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_ANNOUNCEMENT + "')")
    public Result<Announcement> getById(@Parameter(description = "公告ID") @PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    @Operation(summary = "创建公告", description = "创建新的公告，初始状态为草稿")
    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_ANNOUNCEMENT + "')")
    @OperationLog(module = "公告中心", action = "创建", description = "创建公告: #announcement.title", riskLevel = 0)
    public Result<Announcement> create(@RequestBody Announcement announcement) {
        return Result.success(announcementService.createAnnouncement(announcement));
    }

    @Operation(summary = "更新公告", description = "更新公告信息，已发布的公告不可编辑")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_ANNOUNCEMENT + "')")
    @OperationLog(module = "公告中心", action = "更新", description = "更新公告ID: #id", riskLevel = 0)
    public Result<Announcement> update(@Parameter(description = "公告ID") @PathVariable Long id, @RequestBody Announcement announcement) {
        return Result.success(announcementService.updateAnnouncement(id, announcement));
    }

    @Operation(summary = "发布公告", description = "将草稿或定时发布的公告发布为已发布状态")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_ANNOUNCEMENT + "')")
    @OperationLog(module = "公告中心", action = "发布", description = "发布公告ID: #id", riskLevel = 1)
    public Result<Void> publish(@Parameter(description = "公告ID") @PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return Result.success();
    }

    @Operation(summary = "撤回公告", description = "撤回已发布的公告")
    @PutMapping("/{id}/recall")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_ANNOUNCEMENT + "')")
    @OperationLog(module = "公告中心", action = "撤回", description = "撤回公告ID: #id", riskLevel = 1)
    public Result<Void> recall(@Parameter(description = "公告ID") @PathVariable Long id) {
        announcementService.recallAnnouncement(id);
        return Result.success();
    }

    @Operation(summary = "删除公告", description = "删除公告，已发布的公告需先撤回")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_ANNOUNCEMENT + "')")
    @OperationLog(module = "公告中心", action = "删除", description = "删除公告ID: #id", riskLevel = 1)
    public Result<Void> delete(@Parameter(description = "公告ID") @PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
}
