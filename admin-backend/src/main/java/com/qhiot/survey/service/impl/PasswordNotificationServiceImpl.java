package com.qhiot.survey.service.impl;

import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.EmailService;
import com.qhiot.survey.service.PasswordNotificationService;
import com.qhiot.survey.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 密码下发通知服务实现
 *
 * <p>策略：优先短信；以下场景走邮件兜底：</p>
 * <ol>
 *   <li>用户没有手机号；</li>
 *   <li>短信发送失败。</li>
 * </ol>
 *
 * <p>整个流程在 {@code notificationExecutor} 线程池中异步执行，失败仅记录日志，
 * 不影响调用方（密码重置主流程）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordNotificationServiceImpl implements PasswordNotificationService {

    private final SmsService smsService;
    private final EmailService emailService;

    @Override
    @Async("notificationExecutor")
    public void notifyPasswordReset(SysUser user, String newPassword) {
        if (user == null || !StringUtils.hasText(newPassword)) {
            log.warn("[密码通知] 参数为空，跳过下发");
            return;
        }

        String username = StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
        boolean hasPhone = StringUtils.hasText(user.getPhone());
        boolean hasEmail = StringUtils.hasText(user.getEmail());

        if (!hasPhone && !hasEmail) {
            log.error("[密码通知] 用户既无手机号也无邮箱，无法下发新密码: userId={}, username={}",
                    user.getId(), user.getUsername());
            return;
        }

        boolean smsOk = false;
        if (hasPhone) {
            try {
                smsOk = smsService.sendPasswordResetSms(user.getPhone(), username, newPassword);
            } catch (Exception e) {
                log.error("[密码通知] 短信通道异常: userId={}, error={}", user.getId(), e.getMessage(), e);
                smsOk = false;
            }
        }

        if (smsOk) {
            log.info("[密码通知] 短信通道下发成功: userId={}", user.getId());
            return;
        }

        if (!hasEmail) {
            log.error("[密码通知] 短信失败且无邮箱兜底: userId={}, username={}",
                    user.getId(), user.getUsername());
            return;
        }

        boolean emailOk;
        try {
            emailOk = emailService.sendPasswordResetEmail(user.getEmail(), username, newPassword);
        } catch (Exception e) {
            log.error("[密码通知] 邮件通道异常: userId={}, error={}", user.getId(), e.getMessage(), e);
            emailOk = false;
        }

        if (emailOk) {
            log.info("[密码通知] 邮件兜底下发成功: userId={}", user.getId());
        } else {
            log.error("[密码通知] 短信与邮件均失败，请人工通知用户: userId={}, username={}",
                    user.getId(), user.getUsername());
        }
    }
}
