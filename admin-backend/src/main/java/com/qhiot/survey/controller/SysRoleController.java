package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Slf4j
@Tag(name = "角色管理", description = "角色权限管理")
@RestController
@RequestMapping("/api/v1/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    public Result<Page<SysRole>> listByPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(sysRoleService.listByPage(keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取所有启用角色")
    @GetMapping("/list")
    public Result<List<SysRole>> listAll() {
        return Result.success(sysRoleService.listAllEnabled());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "角色管理", action = "创建", description = "创建角色: #role.roleName", riskLevel = 1)
    public Result<SysRole> create(@RequestBody SysRole role) {
        log.info("====== [角色管理] 创建角色请求 - roleName: {} ======", role.getRoleName());
        SysRole result = sysRoleService.createRole(role);
        if (result != null) {
            log.info("====== [角色管理] 角色创建成功 - roleName: {}, roleId: {} ======", role.getRoleName(), result.getId());
        } else {
            log.error("====== [角色管理] 角色创建失败 - roleName: {} ======", role.getRoleName());
        }
        return Result.success(result);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "角色管理", action = "更新", description = "更新角色ID: #id", riskLevel = 1)
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        log.info("====== [角色管理] 更新角色请求 - roleId: {} ======", id);
        SysRole result = sysRoleService.updateRole(id, role);
        if (result != null) {
            log.info("====== [角色管理] 角色更新成功 - roleId: {} ======", id);
        } else {
            log.error("====== [角色管理] 角色更新失败 - roleId: {} ======", id);
        }
        return Result.success(result);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "角色管理", action = "删除", description = "删除角色ID: #id", riskLevel = 2)
    public Result<Void> delete(@PathVariable Long id) {
        log.info("====== [角色管理] 删除角色请求 - roleId: {} ======", id);
        sysRoleService.deleteRole(id);
        log.info("====== [角色管理] 角色删除成功 - roleId: {} ======", id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用角色")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "角色管理", action = "变更状态", description = "变更角色状态, ID: #id, 目标状态: #status", riskLevel = 1)
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("====== [角色管理] 变更角色状态请求 - roleId: {}, targetStatus: {} ======", id, status);
        sysRoleService.toggleStatus(id, status);
        log.info("====== [角色管理] 角色状态变更成功 - roleId: {}, newStatus: {} ======", id, status);
        return Result.success();
    }

    @Operation(summary = "为用户分配角色")
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "角色管理", action = "分配角色", description = "为用户ID: #userId 分配角色", riskLevel = 1)
    public Result<Void> assignRole(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        log.info("====== [角色管理] 分配角色请求 - userId: {}, roleIds: {} ======", userId, roleIds);
        sysRoleService.assignRoleToUser(userId, roleIds);
        log.info("====== [角色管理] 角色分配成功 - userId: {} ======", userId);
        return Result.success();
    }

    @Operation(summary = "获取角色权限配置")
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getRolePermissions(@PathVariable Long id) {
        return Result.success(sysRoleService.getRolePermissions(id));
    }

    @Operation(summary = "更新角色权限配置")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "角色管理", action = "更新权限", description = "更新角色ID: #id 的权限配置", riskLevel = 1)
    public Result<Void> updateRolePermissions(@PathVariable Long id, @RequestBody List<String> permissions) {
        log.info("====== [角色管理] 更新角色权限请求 - roleId: {} ======", id);
        sysRoleService.updateRolePermissions(id, permissions);
        log.info("====== [角色管理] 角色权限更新成功 - roleId: {} ======", id);
        return Result.success();
    }

    @Operation(summary = "获取用户角色列表")
    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getUserRoles(@PathVariable Long userId) {
        return Result.success(sysRoleService.getUserRoles(userId));
    }
}
