package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.MessageCenter;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.MessageCenterService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "分页查询消息列表", description = "获取当前用户的消息列表，支持按类型筛选")
    @GetMapping("/page")
    public Result<Page<MessageCenter>> listByPage(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        return Result.success(messageCenterService.listByPage(userId, type, pageNum, pageSize));
    }

    @Operation(summary = "获取未读消息数量", description = "获取当前用户的未读消息总数")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        return Result.success(messageCenterService.getUnreadCount(userId));
    }

    @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态")
    @PutMapping("/{messageId}/read")
    @OperationLog(module = "消息中心", action = "标记已读", description = "标记消息为已读, 消息ID: #messageId", riskLevel = 0)
    public Result<Void> markAsRead(@Parameter(description = "消息ID") @PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        messageCenterService.markAsRead(messageId, userId);
        return Result.success();
    }

    @Operation(summary = "全部标记为已读", description = "将当前用户的所有未读消息标记为已读")
    @PutMapping("/read-all")
    @OperationLog(module = "消息中心", action = "全部已读", description = "全部标记为已读", riskLevel = 0)
    public Result<Void> batchMarkAsRead() {
        Long userId = getCurrentUserId();
        messageCenterService.batchMarkAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "获取消息详情", description = "根据消息ID获取消息详细内容")
    @GetMapping("/{id}")
    public Result<MessageCenter> getById(@Parameter(description = "消息ID") @PathVariable Long id) {
        return Result.success(messageCenterService.getById(id));
    }

    @Operation(summary = "推送消息给指定角色", description = "管理员向指定角色的所有用户推送消息")
    @PostMapping("/push/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "消息中心", action = "推送角色消息", description = "推送消息给指定角色, 标题: #title", riskLevel = 1)
    public Result<Integer> pushMessageToRoles(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String type,
            @RequestParam(required = false) String roles) {
        int count = messageCenterService.pushMessageToRoles(title, content, type, roles);
        return Result.success(count);
    }

    @Operation(summary = "推送消息给指定用户", description = "管理员向指定用户列表推送消息")
    @PostMapping("/push/users")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "消息中心", action = "推送用户消息", description = "推送消息给指定用户, 标题: #title, 用户数量: #userIds.size()", riskLevel = 1)
    public Result<Integer> pushMessageToUsers(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String type,
            @RequestBody List<Long> userIds) {
        int count = messageCenterService.pushMessageToUsers(title, content, type, userIds);
        return Result.success(count);
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
