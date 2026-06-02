package com.qhiot.survey.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.qhiot.survey.service.SmsService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 通用业务短信服务实现
 *
 * <p>同时支持 Mock 模式（默认，仅写日志）与阿里云模式。
 * 通过配置 {@code aliyun.sms.enabled=true} 启用阿里云通道。</p>
 *
 * <p>失败时不抛异常，由调用方根据返回值决定是否走兜底渠道（如邮件）。</p>
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${aliyun.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:青泓勘察}")
    private String signName;

    @Value("${aliyun.sms.password-template-code:${aliyun.sms.template-code:SMS_123456789}}")
    private String passwordTemplateCode;

    @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    private Client aliyunClient;

    @PostConstruct
    public void init() {
        if (!smsEnabled) {
            log.info("[短信服务] 处于 Mock 模式（aliyun.sms.enabled=false），密码下发短信仅记录日志");
            return;
        }
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint(endpoint);
            this.aliyunClient = new Client(config);
            log.info("[短信服务] 阿里云短信客户端初始化完成: endpoint={}, sign={}, template={}",
                    endpoint, signName, passwordTemplateCode);
        } catch (Exception e) {
            log.error("[短信服务] 阿里云短信客户端初始化失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean sendPasswordResetSms(String phone, String username, String newPassword) {
        if (phone == null || phone.isBlank()) {
            log.warn("[短信服务] 手机号为空，跳过密码下发短信: username={}", username);
            return false;
        }

        if (!smsEnabled || aliyunClient == null) {
            // Mock 模式：仅打日志，便于开发环境验证
            log.info("📱 [Mock短信] 密码重置通知 -> phone={}, username={}, newPassword={}",
                    maskPhone(phone), username, newPassword);
            return true;
        }

        try {
            String templateParam = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    escape(username), escape(newPassword));

            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(passwordTemplateCode)
                    .setTemplateParam(templateParam);

            SendSmsResponse response = aliyunClient.sendSms(request);
            String code = response.getBody() == null ? "NULL" : response.getBody().getCode();
            if ("OK".equals(code)) {
                log.info("[短信服务] 密码重置短信发送成功: phone={}", maskPhone(phone));
                return true;
            }
            log.error("[短信服务] 阿里云返回错误: phone={}, code={}, message={}",
                    maskPhone(phone), code,
                    response.getBody() == null ? "" : response.getBody().getMessage());
            return false;
        } catch (Exception e) {
            log.error("[短信服务] 密码重置短信发送异常: phone={}, error={}",
                    maskPhone(phone), e.getMessage(), e);
            return false;
        }
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
