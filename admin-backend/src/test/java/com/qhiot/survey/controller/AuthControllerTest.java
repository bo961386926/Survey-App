package com.qhiot.survey.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.common.util.JwtUtil;
import com.qhiot.survey.dto.LoginRequest;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.LoginLogService;
import com.qhiot.survey.service.SmsCodeService;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 单元测试
 *
 * <p>覆盖关键认证流程：</p>
 * <ol>
 *   <li>账号密码登录成功</li>
 *   <li>密码错误登录失败（触发 handleLoginFailure）</li>
 *   <li>账号锁定后拒绝登录（5 次失败后 isUserLocked=true）</li>
 *   <li>有效 RefreshToken 刷新成功</li>
 *   <li>过期 RefreshToken 刷新失败</li>
 * </ol>
 *
 * <p>使用 standaloneSetup 避免加载完整 Spring 上下文，所有外部依赖均通过 Mockito mock。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthController 单元测试")
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private SysUserService sysUserService;
    @Mock private SmsCodeService smsCodeService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SysRoleService sysRoleService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private LoginLogService loginLogService;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CAPTCHA_KEY = "test-captcha-key";
    private static final String CAPTCHA_CODE = "1234";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "env", "test");
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 默认验证码校验通过：缓存返回与请求一致
        when(valueOperations.get("captcha:" + CAPTCHA_KEY)).thenReturn(CAPTCHA_CODE);
    }

    /** 构造一个正常可用的测试用户 */
    private SysUser activeUser() {
        SysUser u = new SysUser();
        u.setId(100L);
        u.setUsername("admin");
        u.setPassword("$2a$10$placeholder");
        u.setRealName("管理员");
        u.setStatus(1);
        u.setIsFirstLogin(0);
        u.setLoginFailCount(0);
        return u;
    }

    private LoginRequest validLoginRequest(String username, String password) {
        LoginRequest r = new LoginRequest();
        r.setUsername(username);
        r.setPassword(password);
        r.setCaptcha(CAPTCHA_CODE);
        r.setCaptchaKey(CAPTCHA_KEY);
        return r;
    }

    @Test
    @DisplayName("登录成功 - 正确凭据返回 200 与 Token")
    void testLoginSuccess() throws Exception {
        SysUser user = activeUser();
        when(sysUserService.getUserByUsername("admin")).thenReturn(user);
        when(sysUserService.isUserLocked(user)).thenReturn(false);

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "right-pwd");
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(jwtUtil.generateAccessToken(eq(100L), eq("admin"), anyString()))
                .thenReturn("access-token-abc");
        when(jwtUtil.generateRefreshToken(100L, "admin")).thenReturn("refresh-token-xyz");

        SysRole role = new SysRole();
        role.setRoleCode("admin");
        role.setPermissions("user:list,user:create");
        when(sysRoleService.getUserRoles(100L)).thenReturn(Collections.singletonList(role));

        LoginRequest req = validLoginRequest("admin", "right-pwd");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-abc"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-xyz"))
                .andExpect(jsonPath("$.data.username").value("admin"));

        // 登录成功须更新最近登录时间 & 写成功日志
        verify(sysUserService, times(1)).handleLoginSuccess(user);
        verify(loginLogService, times(1)).logLogin(eq(100L), eq("admin"),
                anyString(), eq(0), any(), anyString(), any());
    }

    @Test
    @DisplayName("登录失败 - 密码错误时调用 handleLoginFailure 并返回错误")
    void testLoginWithWrongPassword() throws Exception {
        SysUser user = activeUser();
        when(sysUserService.getUserByUsername("admin")).thenReturn(user);
        when(sysUserService.isUserLocked(user)).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("密码错误"));

        LoginRequest req = validLoginRequest("admin", "wrong-pwd");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        // 关键验证：失败计数 +1
        verify(sysUserService, times(1)).handleLoginFailure("admin");
        verify(sysUserService, never()).handleLoginSuccess(any());
    }

    @Test
    @DisplayName("登录失败 - 账号已被锁定（达到 5 次失败后）")
    void testLoginRejectedWhenAccountLocked() throws Exception {
        SysUser user = activeUser();
        user.setLoginFailCount(5);
        when(sysUserService.getUserByUsername("admin")).thenReturn(user);
        // 模拟 SysUserService 判定账户已锁
        when(sysUserService.isUserLocked(user)).thenReturn(true);

        // 重新设置验证码，确保读取一次
        when(valueOperations.get("captcha:" + CAPTCHA_KEY)).thenReturn(CAPTCHA_CODE);

        LoginRequest req = validLoginRequest("admin", "any-pwd");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户已被锁定，请稍后再试"));

        // 锁定后绝不应进入认证 / 失败计数流程
        verify(authenticationManager, never()).authenticate(any());
        verify(sysUserService, never()).handleLoginFailure(anyString());
        verify(sysUserService, never()).handleLoginSuccess(any());
    }

    @Test
    @DisplayName("刷新Token - 有效 refreshToken 应返回新的 access/refresh token")
    void testRefreshTokenSuccess() throws Exception {
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getTokenType(refreshToken)).thenReturn("refresh");
        when(jwtUtil.getUserIdFromToken(refreshToken)).thenReturn(100L);
        when(jwtUtil.getUsernameFromToken(refreshToken)).thenReturn("admin");

        SysUser user = activeUser();
        when(sysUserService.getById(100L)).thenReturn(user);

        when(jwtUtil.generateAccessToken(eq(100L), eq("admin"), anyString()))
                .thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(100L, "admin")).thenReturn("new-refresh-token");

        SysRole role = new SysRole();
        role.setRoleCode("admin");
        role.setPermissions("");
        when(sysRoleService.getUserRoles(100L)).thenReturn(Collections.singletonList(role));

        mockMvc.perform(post("/api/v1/auth/refresh").param("refreshToken", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("刷新Token - 过期/非法 refreshToken 应返回错误")
    void testRefreshTokenWithExpiredToken() throws Exception {
        String expired = "expired-refresh-token";
        when(jwtUtil.validateToken(expired)).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/refresh").param("refreshToken", expired))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("刷新Token无效"));

        // 校验未通过时不应签发新 Token
        verify(jwtUtil, never()).generateAccessToken(anyLong(), anyString(), anyString());
        verify(jwtUtil, never()).generateRefreshToken(anyLong(), anyString());
        verify(sysUserService, never()).getById(anyLong());
    }

    @Test
    @DisplayName("刷新Token - Token 类型错误（非 refresh）应返回错误")
    void testRefreshTokenWithWrongType() throws Exception {
        String token = "access-token-not-refresh";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getTokenType(token)).thenReturn("access");

        mockMvc.perform(post("/api/v1/auth/refresh").param("refreshToken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Token类型错误"));

        verify(jwtUtil, never()).generateAccessToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("辅助验证 - 验证码缓存命中后应被立即删除（防重放）")
    void testCaptchaConsumedOnSuccess() throws Exception {
        SysUser user = activeUser();
        when(sysUserService.getUserByUsername("admin")).thenReturn(user);
        when(sysUserService.isUserLocked(user)).thenReturn(false);
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "right-pwd");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("t");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("r");
        when(sysRoleService.getUserRoles(anyLong())).thenReturn(Collections.emptyList());

        LoginRequest req = validLoginRequest("admin", "right-pwd");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(redisTemplate, times(1)).delete("captcha:" + CAPTCHA_KEY);
    }

    @Test
    @DisplayName("登录失败 - 验证码错误（应在认证前拦截）")
    void testLoginWithWrongCaptcha() throws Exception {
        when(valueOperations.get("captcha:" + CAPTCHA_KEY)).thenReturn("9999");

        LoginRequest req = validLoginRequest("admin", "any");
        req.setCaptcha("0000");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("验证码错误"));

        verify(authenticationManager, never()).authenticate(any());
        verify(sysUserService, never()).getUserByUsername(anyString());
    }
}
