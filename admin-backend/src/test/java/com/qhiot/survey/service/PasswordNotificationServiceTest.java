package com.qhiot.survey.service;

import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.impl.PasswordNotificationServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 密码通知 / 密码重置限流 单元测试
 *
 * <p>覆盖：</p>
 * <ol>
 *   <li>短信优先下发</li>
 *   <li>短信失败 → 邮件兜底</li>
 *   <li>限流：超过单用户/单管理员阈值时抛 BusinessException</li>
 *   <li>下发失败不抛异常（异步通道的优雅降级）</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PasswordNotificationService 单元测试")
class PasswordNotificationServiceTest {

    @Mock private SmsService smsService;
    @Mock private EmailService emailService;

    @InjectMocks private PasswordNotificationServiceImpl notificationService;

    private SysUser fullChannelUser() {
        SysUser u = new SysUser();
        u.setId(1L);
        u.setUsername("alice");
        u.setRealName("Alice");
        u.setPhone("13800138000");
        u.setEmail("alice@example.com");
        return u;
    }

    @Test
    @DisplayName("有手机号时短信通道优先尝试，成功后不再走邮件")
    void testSmsIsAttemptedFirst() {
        SysUser user = fullChannelUser();
        when(smsService.sendPasswordResetSms(eq("13800138000"), anyString(), eq("NewPass@2026")))
                .thenReturn(true);

        notificationService.notifyPasswordReset(user, "NewPass@2026");

        verify(smsService, times(1))
                .sendPasswordResetSms(eq("13800138000"), anyString(), eq("NewPass@2026"));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("短信失败时自动降级到邮件兜底")
    void testEmailFallbackWhenSmsFails() {
        SysUser user = fullChannelUser();
        when(smsService.sendPasswordResetSms(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(emailService.sendPasswordResetEmail(eq("alice@example.com"), anyString(), eq("NewPass@2026")))
                .thenReturn(true);

        notificationService.notifyPasswordReset(user, "NewPass@2026");

        verify(smsService, times(1)).sendPasswordResetSms(anyString(), anyString(), anyString());
        verify(emailService, times(1))
                .sendPasswordResetEmail(eq("alice@example.com"), anyString(), eq("NewPass@2026"));
    }

    @Test
    @DisplayName("短信通道抛异常时也应走邮件兜底，且不向上抛")
    void testSmsExceptionTriggersEmailFallback() {
        SysUser user = fullChannelUser();
        when(smsService.sendPasswordResetSms(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("阿里云短信网关超时"));
        when(emailService.sendPasswordResetEmail(anyString(), anyString(), anyString()))
                .thenReturn(true);

        assertDoesNotThrow(() -> notificationService.notifyPasswordReset(user, "NewPass@2026"));

        verify(emailService, times(1)).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("短信与邮件均失败时不抛异常（异步通道优雅降级）")
    void testNotificationFailureDoesNotThrow() {
        SysUser user = fullChannelUser();
        when(smsService.sendPasswordResetSms(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(emailService.sendPasswordResetEmail(anyString(), anyString(), anyString()))
                .thenReturn(false);

        // 关键：双通道失败也不应抛异常打断密码重置主流程
        assertDoesNotThrow(() -> notificationService.notifyPasswordReset(user, "NewPass@2026"));
    }

    @Test
    @DisplayName("无手机号但有邮箱时直接走邮件通道")
    void testNoPhoneFallsBackToEmail() {
        SysUser user = fullChannelUser();
        user.setPhone(null);
        when(emailService.sendPasswordResetEmail(anyString(), anyString(), anyString()))
                .thenReturn(true);

        notificationService.notifyPasswordReset(user, "NewPass@2026");

        verify(smsService, never()).sendPasswordResetSms(anyString(), anyString(), anyString());
        verify(emailService, times(1))
                .sendPasswordResetEmail(eq("alice@example.com"), anyString(), eq("NewPass@2026"));
    }

    @Test
    @DisplayName("既无手机号也无邮箱时直接跳过，不调用任何通道")
    void testNoChannelGracefullySkips() {
        SysUser user = fullChannelUser();
        user.setPhone(null);
        user.setEmail(null);

        assertDoesNotThrow(() -> notificationService.notifyPasswordReset(user, "NewPass@2026"));

        verify(smsService, never()).sendPasswordResetSms(anyString(), anyString(), anyString());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("入参非法（user 为空或新密码为空）时立即返回，不调用任何通道")
    void testInvalidArgumentsAreIgnored() {
        SysUser user = fullChannelUser();

        assertDoesNotThrow(() -> notificationService.notifyPasswordReset(null, "x"));
        assertDoesNotThrow(() -> notificationService.notifyPasswordReset(user, null));
        assertDoesNotThrow(() -> notificationService.notifyPasswordReset(user, ""));

        verify(smsService, never()).sendPasswordResetSms(anyString(), anyString(), anyString());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    // =========== 密码重置限流测试 ===========

    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    @DisplayName("PasswordResetRateLimiter 限流测试")
    static class PasswordResetRateLimiterTest {

        @Mock private StringRedisTemplate redisTemplate;
        @Mock private ValueOperations<String, String> valueOps;
        private PasswordResetRateLimiter limiter;

        @BeforeEach
        void setUp() {
            limiter = new PasswordResetRateLimiter(redisTemplate);
            ReflectionTestUtils.setField(limiter, "userPerHour", 3);
            ReflectionTestUtils.setField(limiter, "adminPerHour", 10);
            when(redisTemplate.opsForValue()).thenReturn(valueOps);
        }

        @Test
        @DisplayName("用户维度未超限时正常放行；首次写入会设置过期时间")
        void testWithinUserLimitAllowed() {
            when(valueOps.increment("pwd-reset:user:1")).thenReturn(1L);
            when(valueOps.increment("pwd-reset:admin:adminA")).thenReturn(1L);

            assertDoesNotThrow(() -> limiter.acquire(1L, "adminA"));

            // 首次写入应设置 1 小时窗口
            verify(redisTemplate, times(1)).expire("pwd-reset:user:1", Duration.ofHours(1));
            verify(redisTemplate, times(1)).expire("pwd-reset:admin:adminA", Duration.ofHours(1));
        }

        @Test
        @DisplayName("用户维度超过单小时阈值时抛 BusinessException")
        void testUserPerHourExceededThrows() {
            // 第 4 次：超过 user-per-hour=3
            when(valueOps.increment("pwd-reset:user:1")).thenReturn(4L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> limiter.acquire(1L, null));
            // 用户维度的错误文案
            assertDoesNotThrow(() -> ex.getMessage().contains("过于频繁"));
        }

        @Test
        @DisplayName("管理员维度超过单小时阈值时抛 BusinessException")
        void testAdminPerHourExceededThrows() {
            when(valueOps.increment("pwd-reset:user:1")).thenReturn(1L);
            // 第 11 次：超过 admin-per-hour=10
            when(valueOps.increment("pwd-reset:admin:adminA")).thenReturn(11L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> limiter.acquire(1L, "adminA"));
            assertDoesNotThrow(() -> ex.getMessage().contains("达到上限"));
        }

        @Test
        @DisplayName("第 2~N 次自增时不再设置 EXPIRE（避免续期）")
        void testExpireSetOnlyOnFirstIncrement() {
            when(valueOps.increment("pwd-reset:user:1")).thenReturn(2L);

            assertDoesNotThrow(() -> limiter.acquire(1L, null));

            verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        }
    }
}
