package com.qhiot.survey.service;

import com.qhiot.survey.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 密码重置限流器（基于 Redis 计数）
 *
 * <p>规则：</p>
 * <ul>
 *   <li>同一目标用户：每小时最多被重置 {@code app.security.password-reset.user-per-hour} 次（默认 3）。</li>
 *   <li>同一管理员：每小时最多触发 {@code app.security.password-reset.admin-per-hour} 次密码下发（默认 10）。</li>
 * </ul>
 *
 * <p>计数采用 INCR + EXPIRE 模式：第一次写入时设置过期时间，后续仅自增，
 * 超过阈值即抛 {@link BusinessException}，由全局异常处理器返回 429/友好错误。</p>
 */
@Slf4j
@Component
public class PasswordResetRateLimiter {

    private static final String USER_KEY_PREFIX = "pwd-reset:user:";
    private static final String ADMIN_KEY_PREFIX = "pwd-reset:admin:";
    private static final Duration WINDOW = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    @Value("${app.security.password-reset.user-per-hour:3}")
    private int userPerHour;

    @Value("${app.security.password-reset.admin-per-hour:10}")
    private int adminPerHour;

    public PasswordResetRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 校验并消费一次配额。配额耗尽时抛 {@link BusinessException}。
     *
     * @param targetUserId 被重置密码的用户 ID
     * @param adminKey     发起重置的管理员标识（如用户名/ID）；为空时跳过管理员维度限流
     */
    public void acquire(Long targetUserId, String adminKey) {
        if (targetUserId != null) {
            long count = increment(USER_KEY_PREFIX + targetUserId);
            if (count > userPerHour) {
                log.warn("[密码重置限流] 用户维度超限: userId={}, count={}/{}",
                        targetUserId, count, userPerHour);
                throw new BusinessException("该用户密码重置过于频繁，请1小时后再试");
            }
        }
        if (adminKey != null && !adminKey.isBlank()) {
            long count = increment(ADMIN_KEY_PREFIX + adminKey);
            if (count > adminPerHour) {
                log.warn("[密码重置限流] 管理员维度超限: admin={}, count={}/{}",
                        adminKey, count, adminPerHour);
                throw new BusinessException("您发起的密码重置次数已达上限，请1小时后再试");
            }
        }
    }

    private long increment(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        long current = value == null ? 0L : value;
        if (current == 1L) {
            redisTemplate.expire(key, WINDOW);
        }
        return current;
    }
}
