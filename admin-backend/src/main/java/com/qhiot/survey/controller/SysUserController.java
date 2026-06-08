package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.common.util.ExcelUtil;
import com.qhiot.survey.dto.CreateUserRequest;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.dto.UpdateUserRequest;
import com.qhiot.survey.dto.UserResponse;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Tag(name = "系统用户管理", description = "用户增删改查、状态管理、密码重置等接口")
@RestController
@RequestMapping(value = "/api/v1/user", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "分页查询用户列表", description = "管理员分页查询用户，支持按用户名和状态筛选，返回包含角色和项目信息")
    @GetMapping(value = "/page", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    public Result<PageResult<UserResponse>> queryUserPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<SysUser> userPage = sysUserService.queryUserPage(username, status, pageNum, pageSize);
        
        // 转换为 UserResponse，添加角色ID列表和负责项目
        List<UserResponse> responses = userPage.getRecords().stream().map(user -> {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setPhone(user.getPhone());
            response.setEmail(user.getEmail());
            response.setStatus(user.getStatus());
            response.setCreateTime(user.getCreateTime());
            response.setUpdateTime(user.getUpdateTime());
            
            // 查询用户的角色ID列表
            List<SysRole> roles = sysRoleService.getUserRoles(user.getId());
            List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
            response.setRoleIds(roleIds);
            
            // 查询用户负责的项目名称
            String projectNames = sysUserService.getUserProjectNames(user.getId());
            response.setProjectName(projectNames);
            
            return response;
        }).toList();
        
        PageResult<UserResponse> resultPage = new PageResult<>(
            responses,
            userPage.getTotal(),
            userPage.getPageNum(),
            userPage.getPageSize(),
            userPage.getTotalPages()
        );
        
        return Result.success(resultPage);
    }

    @Operation(summary = "获取用户列表", description = "获取所有用户的简要列表，不含角色和项目详情")
    @GetMapping(value = "/list", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    public Result<List<SysUser>> getUserList() {
        List<SysUser> users = sysUserService.getUserList();
        return Result.success(users);
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取详细信息，包含角色ID列表")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    public Result<UserResponse> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        // 查询用户角色ID列表
        List<SysRole> roles = sysRoleService.getUserRoles(user.getId());
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        response.setRoleIds(roleIds);
        return Result.success(response);
    }

    @Operation(summary = "创建用户", description = "创建新用户并分配角色，默认启用且首次登录需改密")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    @OperationLog(module = "用户管理", action = "创建", description = "创建用户: #request.username", riskLevel = 1)
    public Result<Boolean> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("====== [用户管理] 创建用户请求 - username: {}, realName: {}, roleIds: {} ======", 
            request.getUsername(), request.getRealName(), request.getRoleIds());
        
        // 创建用户基本信息
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // 默认启用
        user.setIsFirstLogin(1); // 首次登录需要修改密码
        
        boolean success = sysUserService.createUser(user);
        
        if (success && request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            // 分配角色（支持多角色）
            sysRoleService.assignRoleToUser(user.getId(), request.getRoleIds());
            log.info("====== [用户管理] 为用户分配角色 - userId: {}, roleIds: {} ======", 
                user.getId(), request.getRoleIds());
        }
        
        if (success) {
            log.info("====== [用户管理] 用户创建成功 - username: {}, userId: {} ======", 
                request.getUsername(), user.getId());
        } else {
            log.error("====== [用户管理] 用户创建失败 - username: {} ======", request.getUsername());
        }
        
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新用户", description = "更新用户信息，支持同时更新密码和角色分配")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    @OperationLog(module = "用户管理", action = "更新", description = "更新用户: #request.id", riskLevel = 1)
    public Result<Boolean> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        log.info("====== [用户管理] 更新用户请求 - userId: {}, realName: {}, roleIds: {} ======", 
            request.getId(), request.getRealName(), request.getRoleIds());
        
        // 更新用户基本信息
        SysUser user = new SysUser();
        user.setId(request.getId());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        // 如果提供了新密码，则更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("====== [用户管理] 同时更新密码 - userId: {} ======", request.getId());
        }
        
        boolean success = sysUserService.updateUser(user);
        
        if (success && request.getRoleIds() != null) {
            // 重新分配角色（支持多角色）
            sysRoleService.assignRoleToUser(request.getId(), request.getRoleIds());
            log.info("====== [用户管理] 重新分配角色 - userId: {}, roleIds: {} ======", 
                request.getId(), request.getRoleIds());
        }
        
        if (success) {
            log.info("====== [用户管理] 用户更新成功 - userId: {} ======", request.getId());
        } else {
            log.error("====== [用户管理] 用户更新失败 - userId: {} ======", request.getId());
        }
        
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除用户", description = "删除指定用户，高风险操作")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    @OperationLog(module = "用户管理", action = "删除", description = "删除用户ID: #id", riskLevel = 2)
    public Result<Boolean> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        boolean success = sysUserService.deleteUser(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新用户状态", description = "启用或禁用用户账号，0禁用/1启用")
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    @OperationLog(module = "用户管理", action = "更新状态", description = "更新用户状态, ID: #id, 状态: #status", riskLevel = 1)
    public Result<Boolean> updateUserStatus(@Parameter(description = "用户ID") @PathVariable Long id,
                                             @Parameter(description = "状态：0禁用/1启用") @RequestParam Integer status) {
        boolean success = sysUserService.updateUserStatus(id, status);
        return success ? Result.success(true) : Result.error("更新状态失败");
    }

    @Operation(summary = "重置用户密码", description = "管理员重置用户密码，新密码需符合复杂度要求，系统自动下发短信/邮件通知")
    @PutMapping("/reset-password/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    @OperationLog(module = "用户管理", action = "重置密码", description = "重置用户密码, ID: #id", riskLevel = 2)
    public Result<Boolean> resetPassword(
        @Parameter(description = "用户ID") @PathVariable Long id, 
        @Valid @RequestBody com.qhiot.survey.dto.ResetPasswordRequest request
    ) {
        log.info("====== [用户管理] 重置密码请求 - userId: {} ======", id);
        // 服务层负责限流 + 加密落库 + 异步下发（短信优先，邮件兜底）
        boolean success = sysUserService.resetPasswordAndNotify(id, request.getNewPassword(), null);
        if (success) {
            log.info("====== [用户管理] 密码重置成功 - userId: {} ======", id);
        } else {
            log.error("====== [用户管理] 密码重置失败 - userId: {} ======", id);
        }
        return success ? Result.success(true) : Result.error("重置密码失败");
    }

    @Operation(summary = "导出用户数据", description = "导出用户数据为Excel文件")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] excelBytes = sysUserService.exportUsers();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "users.xlsx");
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "导入用户数据", description = "从Excel文件批量导入用户数据")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_USER + "')")
    @OperationLog(module = "用户管理", action = "导入", description = "批量导入用户数据", riskLevel = 1)
    public Result<Map<String, Integer>> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        // 读取Excel文件，6列：用户名、真实姓名、手机号、邮箱、角色、状态
        List<Map<Integer, String>> data = ExcelUtil.readExcel(file, 6);
        
        Map<String, Integer> result = sysUserService.importUsers(data);
        return Result.success(result);
    }
}
