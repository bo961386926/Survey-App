package com.qhiot.survey.controller;

import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.common.util.JwtUtil;
import com.qhiot.survey.dto.*;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SmsCodeService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysUserService sysUserService;
    private final SmsCodeService smsCodeService;
    private final PasswordEncoder passwordEncoder;
    private final SysRoleService sysRoleService;
    private final StringRedisTemplate redisTemplate;
    
    @Value("${app.env:prod}")
    private String env;
    
    // 验证码Redis Key前缀
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    // 验证码过期时间（分钟）
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;
    
    // 幂等性Token Redis Key前缀
    private static final String IDEMPOTENT_TOKEN_KEY = "idempotent:token:";
    // 幂等性Token过期时间（分钟）
    private static final long IDEMPOTENT_TOKEN_EXPIRE_MINUTES = 5;

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        try {
            // 生成随机验证码 (4位数字)
            String code = String.valueOf((int)((Math.random() * 9000) + 1000));
            String key = UUID.randomUUID().toString().replace("-", "");
            
            // 存入Redis缓存，5分钟过期
            String redisKey = CAPTCHA_KEY_PREFIX + key;
            redisTemplate.opsForValue().set(redisKey, code, CAPTCHA_EXPIRE_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            
            // 生成图片
            int width = 120;
            int height = 40;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            
            // 背景
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            
            // 边框
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(0, 0, width - 1, height - 1);
            
            // 验证码文字
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString(code, 30, 30);
            
            // 干扰线
            for (int i = 0; i < 5; i++) {
                g.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
                int x1 = (int)(Math.random() * width);
                int y1 = (int)(Math.random() * height);
                int x2 = (int)(Math.random() * width);
                int y2 = (int)(Math.random() * height);
                g.drawLine(x1, y1, x2, y2);
            }
            
            g.dispose();
            
            // 转Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
            
            Map<String, String> result = new HashMap<>();
            result.put("key", key);
            result.put("image", base64);
            
            // 仅在开发/测试环境返回验证码，便于调试
            if ("dev".equals(env) || "test".equals(env)) {
                result.put("code", code);
                result.put("_warning", "开发环境：验证码仅用于调试，生产环境不会返回");
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("生成验证码失败: " + e.getMessage());
        }
    }

    @Operation(summary = "用户登录（账号密码）")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 验证图形验证码
            if (request.getCaptchaKey() == null || request.getCaptcha() == null) {
                return Result.error("请输入验证码");
            }
            
            // 从Redis获取验证码
            String redisKey = CAPTCHA_KEY_PREFIX + request.getCaptchaKey();
            String cachedCode = redisTemplate.opsForValue().get(redisKey);
            
            if (cachedCode == null) {
                return Result.error("验证码已过期，请刷新");
            }
            
            if (!cachedCode.equals(request.getCaptcha())) {
                redisTemplate.delete(redisKey); // 验证失败删除
                return Result.error("验证码错误");
            }
            
            redisTemplate.delete(redisKey); // 验证成功删除
            
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
            LoginResponse response = buildLoginResponse(user, accessToken, refreshToken);

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
            LoginResponse response = buildLoginResponse(user, accessToken, refreshToken);

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
    
    @Operation(summary = "获取幂等性Token")
    @GetMapping("/idempotent-token")
    public Result<String> getIdempotentToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        String redisKey = IDEMPOTENT_TOKEN_KEY + token;
        redisTemplate.opsForValue().set(
            redisKey, 
            "1", 
            IDEMPOTENT_TOKEN_EXPIRE_MINUTES, 
            java.util.concurrent.TimeUnit.MINUTES
        );
        return Result.success(token);
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
    public Result<Void> resetPassword(@Valid @RequestBody SmsResetPasswordRequest request) {
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

            LoginResponse response = buildLoginResponse(user, newAccessToken, newRefreshToken);

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("刷新Token失败: " + e.getMessage());
        }
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(SysUser user, String accessToken, String refreshToken) {
        String loginWarning = null;
        if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
            loginWarning = "您的账号近期有登录失败记录，请注意账号安全";
        }
        
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getIsFirstLogin() == 1,
                loginWarning
        );
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 清除Security上下文
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/getUserInfo")
    public Result<UserInfoResponse> getUserInfo() {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SysUser user = sysUserService.getUserByUsername(username);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 获取用户角色
            String[] roles;
            if (user.getRole() != null) {
                SysRole role = sysRoleService.getById(user.getRole());
                if (role != null && role.getRoleCode() != null) {
                    roles = new String[]{role.getRoleCode()};
                } else {
                    roles = new String[]{"user"};
                }
            } else {
                roles = new String[]{"user"};
            }

            // 暂时返回空按钮权限列表
            String[] buttons = new String[]{};

            UserInfoResponse response = new UserInfoResponse(
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    user.getRealName(),
                    roles,
                    buttons
            );

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
