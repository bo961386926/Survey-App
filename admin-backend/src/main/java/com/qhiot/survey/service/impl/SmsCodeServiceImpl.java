package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.mapper.SysUserMapper;
import com.qhiot.survey.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现
 * 
 * 注意：实际项目中需要接入真实的短信服务（如阿里云、腾讯云等）
 * 当前实现使用Redis存储验证码，仅用于开发测试
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    private final StringRedisTemplate redisTemplate;
    private final SysUserMapper sysUserMapper;

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

    @Override
    public boolean sendSmsCode(String phone, String scene) {
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

        // TODO: 接入真实短信服务，发送验证码
        log.info("发送短信验证码: phone={}, scene={}, code={}", phone, scene, code);

        return true;
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
    public SysUser getUserByPhone(String phone) {
        return sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("phone", phone));
    }
}