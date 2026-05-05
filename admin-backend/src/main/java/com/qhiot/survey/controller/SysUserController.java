package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.util.ExcelUtil;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 系统用户管理控制器
 */
@Tag(name = "系统用户管理", description = "用户增删改查、状态管理、密码重置等接口")
@RestController
@RequestMapping(value = "/api/v1/user", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "分页查询用户列表")
    @GetMapping(value = "/page", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<SysUser>> queryUserPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(sysUserService.queryUserPage(username, role, status, pageNum, pageSize));
    }

    @Operation(summary = "获取用户列表")
    @GetMapping(value = "/list", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysUser>> getUserList() {
        List<SysUser> users = sysUserService.getUserList();
        return Result.success(users);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }

    @Operation(summary = "创建用户")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> createUser(@RequestBody SysUser user) {
        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean success = sysUserService.createUser(user);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新用户")
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateUser(@RequestBody SysUser user) {
        // 如果更新密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }
        boolean success = sysUserService.updateUser(user);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        boolean success = sysUserService.deleteUser(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = sysUserService.updateUserStatus(id, status);
        return success ? Result.success(true) : Result.error("更新状态失败");
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> resetPassword(
        @PathVariable Long id, 
        @Valid @RequestBody com.qhiot.survey.dto.ResetPasswordRequest request
    ) {
        boolean success = sysUserService.resetPassword(id, request.getNewPassword());
        return success ? Result.success(true) : Result.error("重置密码失败");
    }

    @Operation(summary = "根据角色获取用户列表")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysUser>> getUsersByRole(@PathVariable Integer role) {
        List<SysUser> users = sysUserService.getUsersByRole(role);
        return Result.success(users);
    }

    @Operation(summary = "导出用户数据")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] excelBytes = sysUserService.exportUsers();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "users.xlsx");
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "导入用户数据")
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Integer>> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        // 读取Excel文件，6列：用户名、真实姓名、手机号、邮箱、角色、状态
        List<Map<Integer, String>> data = ExcelUtil.readExcel(file, 6);
        
        Map<String, Integer> result = sysUserService.importUsers(data);
        return Result.success(result);
    }
}
