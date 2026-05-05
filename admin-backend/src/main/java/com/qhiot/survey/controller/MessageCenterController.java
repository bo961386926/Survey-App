package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.MessageCenter;
import com.qhiot.survey.service.MessageCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息中心控制器
 */
@Tag(name = "消息中心", description = "消息通知管理")
@RestController
@RequestMapping("/api/v1/message")
public class MessageCenterController {

    @Autowired
    private MessageCenterService messageCenterService;

    @Operation(summary = "分页查询消息列表")
    @GetMapping("/page")
    public Result<Page<MessageCenter>> listByPage(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 实际应该从SecurityContext获取当前用户ID
        Long userId = 1L;
        return Result.success(messageCenterService.listByPage(userId, type, pageNum, pageSize));
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        // 实际应该从SecurityContext获取当前用户ID
        Long userId = 1L;
        return Result.success(messageCenterService.getUnreadCount(userId));
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/{messageId}/read")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        // 实际应该从SecurityContext获取当前用户ID
        Long userId = 1L;
        messageCenterService.markAsRead(messageId, userId);
        return Result.success();
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public Result<Void> batchMarkAsRead() {
        // 实际应该从SecurityContext获取当前用户ID
        Long userId = 1L;
        messageCenterService.batchMarkAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "获取消息详情")
    @GetMapping("/{id}")
    public Result<MessageCenter> getById(@PathVariable Long id) {
        return Result.success(messageCenterService.getById(id));
    }

    @Operation(summary = "推送消息给指定角色")
    @PostMapping("/push/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Integer> pushMessageToRoles(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String type,
            @RequestParam(required = false) String roles) {
        int count = messageCenterService.pushMessageToRoles(title, content, type, roles);
        return Result.success(count);
    }

    @Operation(summary = "推送消息给指定用户")
    @PostMapping("/push/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Integer> pushMessageToUsers(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String type,
            @RequestBody List<Long> userIds) {
        int count = messageCenterService.pushMessageToUsers(title, content, type, userIds);
        return Result.success(count);
    }
}
