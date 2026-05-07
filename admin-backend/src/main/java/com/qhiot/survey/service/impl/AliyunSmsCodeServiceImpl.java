package com.qhiot.survey.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.qhiot.survey.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 阿里云短信服务实现
 * 
 * 使用条件：在application.yml中配置 aliyun.sms.enabled=true
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "aliyun.sms.enabled", havingValue = "true", matchIfMissing = false)
public class AliyunSmsCodeServiceImpl implements com.qhiot.survey.service.SmsCodeService {

    private final StringRedisTemplate redisTemplate;

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:青泓勘察}")
    private String signName;

    @Value("${aliyun.sms.template-code:SMS_123456789}")
    private String templateCode;

    @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    /**
     * 验证码前缀
     */
    private static final String SMS_CODE_PREFIX = "sms:code:";

    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 发送间隔（秒）
     */
    private static final int SEND_INTERVAL_SECONDS = 60;

    public AliyunSmsCodeServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean sendSmsCode(String phone, String scene) throws BusinessException {
        // 检查发送间隔
        String intervalKey = SMS_CODE_PREFIX + "interval:" + scene + ":" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(intervalKey))) {
            throw new BusinessException("请勿频繁发送验证码，请60秒后再试");
        }

        // 生成6位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 存储验证码
        String codeKey = SMS_CODE_PREFIX + scene + ":" + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送间隔
        redisTemplate.opsForValue().set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 发送短信
        try {
            sendSmsViaAliyun(phone, code);
            log.info("阿里云短信发送成功: phone={}, scene={}", phone, scene);
            return true;
        } catch (Exception e) {
            log.error("阿里云短信发送失败: phone={}, scene={}, error={}", phone, scene, e.getMessage(), e);
            // 删除已存储的验证码
            redisTemplate.delete(codeKey);
            redisTemplate.delete(intervalKey);
            throw new BusinessException("短信发送失败，请稍后再试");
        }
    }

    @Override
    public boolean verifySmsCode(String phone, String code, String scene) {
        String codeKey = SMS_CODE_PREFIX + scene + ":" + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            throw new BusinessException("验证码已过期");
        }

        if (!storedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        // 验证成功后删除验证码
        redisTemplate.delete(codeKey);
        return true;
    }

    @Override
    public com.qhiot.survey.entity.SysUser getUserByPhone(String phone) {
        // 这个方法需要在实现类中注入Mapper
        // 暂时返回null，实际使用时需要重构
        return null;
    }

    /**
     * 通过阿里云发送短信
     */
    private void sendSmsViaAliyun(String phone, String code) throws Exception {
        Config config = new Config()
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret)
            .setEndpoint(endpoint);
        
        Client client = new Client(config);

        SendSmsRequest request = new SendSmsRequest()
            .setPhoneNumbers(phone)
            .setSignName(signName)
            .setTemplateCode(templateCode)
            .setTemplateParam("{\"code\":\"" + code + "\"}");

        SendSmsResponse response = client.sendSms(request);
        
        if (!"OK".equals(response.getBody().getCode())) {
            throw new RuntimeException("阿里云短信API返回错误: " + response.getBody().getMessage());
        }
    }
}
