package com.qhiot.survey.controller;

import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.common.util.JwtUtil;
import com.qhiot.survey.dto.*;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SmsCodeService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysUserService sysUserService;
    private final SmsCodeService smsCodeService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "用户登录（账号密码）")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 检查用户是否存在
            SysUser user = sysUserService.getUserByUsername(request.getUsername());
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                return Result.error("账号已被禁用");
            }

            // 检查用户是否被锁定
            if (sysUserService.isUserLocked(user)) {
                return Result.error("用户已被锁定，请稍后再试");
            }

            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 认证成功，处理登录成功逻辑
            sysUserService.handleLoginSuccess(user);

            // 生成Token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), "internal");
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            // 构建响应
            String loginWarning = null;
            if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
                loginWarning = "您的账号近期有登录失败记录，请注意账号安全";
            }
            
            LoginResponse response = new LoginResponse(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getRole(),
                    user.getIsFirstLogin() == 1,
                    loginWarning
            );

            return Result.success(response);
        } catch (BadCredentialsException e) {
            // 登录失败，处理失败逻辑
            sysUserService.handleLoginFailure(request.getUsername());
            return Result.error("用户名或密码错误");
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/sms-login")
    public Result<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        try {
            // 验证短信验证码
            smsCodeService.verifySmsCode(request.getPhone(), request.getCode(), "login");

            // 根据手机号获取用户
            SysUser user = smsCodeService.getUserByPhone(request.getPhone());
            if (user == null) {
                return Result.error("该手机号未注册");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                return Result.error("账号已被禁用");
            }

            // 检查用户是否被锁定
            if (sysUserService.isUserLocked(user)) {
                return Result.error("用户已被锁定，请稍后再试");
            }

            // 生成Token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), "internal");
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            // 更新最后登录时间
            sysUserService.handleLoginSuccess(user);

            // 构建响应
            String loginWarning = null;
            if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
                loginWarning = "您的账号近期有登录失败记录，请注意账号安全";
            }
            
            LoginResponse response = new LoginResponse(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getRole(),
                    user.getIsFirstLogin() == 1,
                    loginWarning
            );

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("短信验证码登录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/send-sms-code")
    public Result<Boolean> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        try {
            // 如果是重置密码场景，检查手机号是否存在
            if ("reset".equals(request.getScene())) {
                SysUser user = smsCodeService.getUserByPhone(request.getPhone());
                if (user == null) {
                    return Result.error("该手机号未注册");
                }
            }
            
            boolean success = smsCodeService.sendSmsCode(request.getPhone(), request.getScene());
            return success ? Result.success(true) : Result.error("发送失败");
        } catch (Exception e) {
            return Result.error("发送短信验证码失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SysUser user = sysUserService.getUserByUsername(username);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证旧密码
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, request.getOldPassword())
                );
            } catch (BadCredentialsException e) {
                return Result.error("原密码错误");
            }

            // 更新密码
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            updateUser.setIsFirstLogin(0);
            sysUserService.updateById(updateUser);

            return Result.success();
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重置密码（通过短信验证码）")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // 验证短信验证码
            smsCodeService.verifySmsCode(request.getPhone(), request.getCode(), "reset");

            // 根据手机号获取用户
            SysUser user = smsCodeService.getUserByPhone(request.getPhone());
            if (user == null) {
                return Result.error("该手机号未注册");
            }

            // 更新密码
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            updateUser.setIsFirstLogin(0);
            sysUserService.updateById(updateUser);

            return Result.success();
        } catch (Exception e) {
            return Result.error("重置密码失败: " + e.getMessage());
        }
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        try {
            if (!jwtUtil.validateToken(refreshToken)) {
                return Result.error("刷新Token无效");
            }

            if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
                return Result.error("Token类型错误");
            }

            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);

            SysUser user = sysUserService.getById(userId);
            if (user == null || user.getStatus() == 0) {
                return Result.error("用户不存在或已被禁用");
            }

            String newAccessToken = jwtUtil.generateAccessToken(userId, username, "internal");
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

            String loginWarning = null;
            if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
                loginWarning = "您的账号近期有登录失败记录，请注意账号安全";
            }
            
            LoginResponse response = new LoginResponse(
                    newAccessToken,
                    newRefreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getRole(),
                    user.getIsFirstLogin() == 1,
                    loginWarning
            );

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("刷新Token失败: " + e.getMessage());
        }
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 清除Security上下文
        SecurityContextHolder.clearContext();
        return Result.success();
    }
}