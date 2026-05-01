package com.qhiot.survey.service;

/**
 * 短信验证码服务接口
 */
public interface SmsCodeService {

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param scene 场景：login-登录, reset-重置密码
     * @return 是否发送成功
     */
    boolean sendSmsCode(String phone, String scene);

    /**
     * 验证短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @param scene 场景
     * @return 是否验证成功
     */
    boolean verifySmsCode(String phone, String code, String scene);

    /**
     * 根据手机号获取用户
     * @param phone 手机号
     * @return 用户信息
     */
    com.qhiot.survey.entity.SysUser getUserByPhone(String phone);
}