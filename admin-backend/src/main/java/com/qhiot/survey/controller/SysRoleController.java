package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色权限管理")
@RestController
@RequestMapping("/api/role")
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
    public Result<SysRole> create(@RequestBody SysRole role) {
        return Result.success(sysRoleService.createRole(role));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        return Result.success(sysRoleService.updateRole(id, role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用角色")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysRoleService.toggleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "为用户分配角色")
    @PostMapping("/assign")
    public Result<Void> assignRole(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        sysRoleService.assignRoleToUser(userId, roleIds);
        return Result.success();
    }

    @Operation(summary = "获取角色权限配置")
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getRolePermissions(@PathVariable Long id) {
        return Result.success(sysRoleService.getRolePermissions(id));
    }

    @Operation(summary = "更新角色权限配置")
    @PutMapping("/{id}/permissions")
    public Result<Void> updateRolePermissions(@PathVariable Long id, @RequestBody List<String> permissions) {
        sysRoleService.updateRolePermissions(id, permissions);
        return Result.success();
    }

    @Operation(summary = "获取用户角色列表")
    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getUserRoles(@PathVariable Long userId) {
        return Result.success(sysRoleService.getUserRoles(userId));
    }
}
