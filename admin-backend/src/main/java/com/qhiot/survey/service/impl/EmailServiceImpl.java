package com.qhiot.survey.service.impl;

import com.qhiot.survey.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 邮件服务实现
 *
 * <p>仅当 Spring Boot 自动装配出 {@link JavaMailSender} 时才会真正发送邮件，
 * 否则降级为 Mock 模式（仅记录日志），保证开发环境不依赖 SMTP。</p>
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.from:${spring.mail.username:no-reply@qhiot.com}}")
    private String fromAddress;

    @Value("${app.notification.email.from-name:青泓勘察系统}")
    private String fromName;

    public EmailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public boolean sendPasswordResetEmail(String toEmail, String username, String newPassword) {
        if (!StringUtils.hasText(toEmail)) {
            log.warn("[邮件服务] 收件人邮箱为空，跳过密码下发邮件: username={}", username);
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        String html = buildPasswordResetHtml(username, newPassword);

        if (mailSender == null) {
            log.info("📧 [Mock邮件] 未配置 spring.mail.host，仅记录密码重置邮件: to={}, username={}",
                    maskEmail(toEmail), username);
            log.debug("[Mock邮件] 邮件正文: {}", html);
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject("【青泓勘察】您的登录密码已重置");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[邮件服务] 密码重置邮件发送成功: to={}", maskEmail(toEmail));
            return true;
        } catch (Exception e) {
            log.error("[邮件服务] 密码重置邮件发送失败: to={}, error={}",
                    maskEmail(toEmail), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构造密码重置邮件 HTML 模板
     */
    private String buildPasswordResetHtml(String username, String newPassword) {
        return "<!DOCTYPE html><html><body style=\"font-family:Helvetica,Arial,sans-serif;color:#1f2937;line-height:1.6;\">"
                + "<div style=\"max-width:560px;margin:24px auto;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;\">"
                + "<div style=\"background:#1d4ed8;color:#ffffff;padding:16px 24px;font-size:18px;\">青泓勘察 · 密码重置通知</div>"
                + "<div style=\"padding:24px;\">"
                + "<p>您好，<strong>" + escapeHtml(username) + "</strong>：</p>"
                + "<p>您的账号密码已由系统管理员重置。请使用以下新密码登录后<strong>立即修改</strong>：</p>"
                + "<div style=\"font-size:20px;font-weight:bold;letter-spacing:1px;background:#f3f4f6;border:1px dashed #9ca3af;padding:12px 16px;border-radius:6px;margin:16px 0;\">"
                + escapeHtml(newPassword)
                + "</div>"
                + "<p style=\"color:#6b7280;font-size:13px;\">若本次操作非您本人发起，请立即联系系统管理员。</p>"
                + "<p style=\"color:#6b7280;font-size:13px;margin-top:24px;\">本邮件由系统自动发送，请勿直接回复。</p>"
                + "</div></div></body></html>";
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        String name = email.substring(0, at);
        String domain = email.substring(at);
        if (name.length() <= 2) {
            return name.charAt(0) + "*" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }
}
