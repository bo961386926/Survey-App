package com.qhiot.survey.controller;

import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.common.util.IpUtils;
import com.qhiot.survey.common.util.JwtUtil;
import com.qhiot.survey.dto.*;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.service.LoginLogService;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SmsCodeService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;
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
    private final LoginLogService loginLogService;
    
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

    @Operation(summary = "用户登录(账号密码)")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
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
                // 记录登录失败日志
                loginLogService.logLogin(null, request.getUsername(), "internal", 1, 
                    "用户名不存在", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
                return Result.error("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                // 记录登录失败日志
                loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 1, 
                    "账号已被禁用", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
                return Result.error("账号已被禁用");
            }

            // 检查用户是否被锁定
            if (sysUserService.isUserLocked(user)) {
                // 记录登录失败日志
                loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 1, 
                    "用户已被锁定", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
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

            // 记录登录成功日志
            loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 0, 
                null, IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));

            // 生成Token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), "internal");
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            // 构建响应
            LoginResponse response = buildLoginResponse(user, accessToken, refreshToken);

            return Result.success(response);
        } catch (BadCredentialsException e) {
            // 登录失败，处理失败逻辑
            sysUserService.handleLoginFailure(request.getUsername());
            
            // 记录登录失败日志
            SysUser user = sysUserService.getUserByUsername(request.getUsername());
            if (user != null) {
                loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 1, 
                    "密码错误", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
            } else {
                loginLogService.logLogin(null, request.getUsername(), "internal", 1, 
                    "密码错误", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
            }
            
            return Result.error("用户名或密码错误");
        } catch (Exception e) {
            // 记录登录失败日志
            SysUser user = sysUserService.getUserByUsername(request.getUsername());
            if (user != null) {
                loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 1, 
                    "登录异常: " + e.getMessage(), IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
            } else {
                loginLogService.logLogin(null, request.getUsername(), "internal", 1, 
                    "登录异常: " + e.getMessage(), IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
            }
            
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/sms-login")
    public Result<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request, HttpServletRequest httpRequest) {
        try {
            // 验证短信验证码
            smsCodeService.verifySmsCode(request.getPhone(), request.getCode(), "login");

            // 根据手机号获取用户
            SysUser user = smsCodeService.getUserByPhone(request.getPhone());
            if (user == null) {
                // 记录登录失败日志
                loginLogService.logLogin(null, request.getPhone(), "internal", 1, 
                    "手机号未注册", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
                return Result.error("该手机号未注册");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                // 记录登录失败日志
                loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 1, 
                    "账号已被禁用", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
                return Result.error("账号已被禁用");
            }

            // 检查用户是否被锁定
            if (sysUserService.isUserLocked(user)) {
                // 记录登录失败日志
                loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 1, 
                    "用户已被锁定", IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
                return Result.error("用户已被锁定，请稍后再试");
            }

            // 生成Token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), "internal");
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            // 更新最后登录时间
            sysUserService.handleLoginSuccess(user);

            // 记录登录成功日志
            loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 0, 
                null, IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));

            // 构建响应
            LoginResponse response = buildLoginResponse(user, accessToken, refreshToken);

            return Result.success(response);
        } catch (Exception e) {
            // 记录登录失败日志
            SysUser user = smsCodeService.getUserByPhone(request.getPhone());
            if (user != null) {
                loginLogService.logLogin(user.getId(), user.getUsername(), "internal", 1, 
                    "短信登录异常: " + e.getMessage(), IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
            } else {
                loginLogService.logLogin(null, request.getPhone(), "internal", 1, 
                    "短信登录异常: " + e.getMessage(), IpUtils.getClientIp(httpRequest), IpUtils.getUserAgent(httpRequest));
            }
            
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
    @OperationLog(module = "认证管理", action = "修改密码", description = "用户修改密码", riskLevel = 2)
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
    @OperationLog(module = "认证管理", action = "重置密码", description = "用户重置密码", riskLevel = 2)
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
        
        // 从数据库查询用户的真实角色列表
        List<SysRole> roles = sysRoleService.getUserRoles(user.getId());
        
        // 提取角色编码
        String[] roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .filter(code -> code != null && !code.isEmpty())
                .toArray(String[]::new);
        if (roleCodes.length == 0) {
            roleCodes = new String[]{"user"};
        }
        
        // 聚合所有角色的权限（去重）- 同时清理 JSON 格式遗留
        java.util.Set<String> permissionSet = new java.util.HashSet<>();
        for (SysRole role : roles) {
            String perms = role.getPermissions();
            if (perms != null && !perms.isEmpty()) {
                for (String p : perms.split(",")) {
                    String trimmed = p.trim().replace("[", "").replace("]", "").replace("\"", "");
                    if (!trimmed.isEmpty()) {
                        permissionSet.add(trimmed);
                    }
                }
            }
        }
        // 展开通配符
        String[] permissions = com.qhiot.survey.common.util.PermissionRegistry.expandWildcard(
                permissionSet.toArray(new String[0]));
        
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                roleCodes,
                permissions,
                user.getIsFirstLogin() == 1,
                loginWarning
        );
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    @OperationLog(module = "认证管理", action = "退出登录", description = "用户退出登录", riskLevel = 0)
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

            // ✅ 关键修复：从 sys_user_role 表查询用户的多个角色（支持多角色）
            List<SysRole> roles = sysRoleService.getUserRoles(user.getId());
            
            String[] roleCodes;
            if (roles != null && !roles.isEmpty()) {
                // 将角色实体转换为角色编码数组
                roleCodes = roles.stream()
                    .map(SysRole::getRoleCode)
                    .filter(code -> code != null && !code.isEmpty())
                    .toArray(String[]::new);
                
                System.out.println("====== [AuthController] getUserInfo - userId: " + user.getId() + ", roles: " + java.util.Arrays.toString(roleCodes) + " ======");
            } else {
                // 如果没有分配角色，给默认角色
                roleCodes = new String[]{"user"};
                System.out.println("====== [AuthController] getUserInfo - userId: " + user.getId() + ", no roles assigned, using default ======");
            }

            // 聚合所有角色的权限（去重）
            java.util.Set<String> permSet = new java.util.HashSet<>();
            for (SysRole role : roles) {
                String perms = role.getPermissions();
                if (perms != null && !perms.isEmpty()) {
                    for (String p : perms.split(",")) {
                        String trimmed = p.trim().replace("[", "").replace("]", "").replace("\"", "");
                        if (!trimmed.isEmpty()) permSet.add(trimmed);
                    }
                }
            }
            String[] permissions = permSet.toArray(new String[0]);

            String[] buttons = new String[]{};

            UserInfoResponse response = UserInfoResponse.create(
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    user.getRealName(),
                    roleCodes,
                    permissions,
                    buttons
            );

            return Result.success(response);
        } catch (Exception e) {
            System.err.println("====== [AuthController] getUserInfo error: " + e.getMessage() + " ======");
            e.printStackTrace();
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
