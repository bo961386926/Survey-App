package com.qhiot.survey.service;

/**
 * 通用短信发送服务
 * 用于发送业务通知短信（如重置密码后下发新密码等）
 *
 * <p>与 {@link SmsCodeService} 区分：SmsCodeService 仅负责验证码场景，
 * 本服务负责通用业务短信。</p>
 */
public interface SmsService {

    /**
     * 发送密码重置短信
     *
     * @param phone       手机号
     * @param username    用户名（用于模板替换）
     * @param newPassword 新密码（明文）
     * @return 是否发送成功；失败时不抛异常，仅记录日志
     */
    boolean sendPasswordResetSms(String phone, String username, String newPassword);
}
