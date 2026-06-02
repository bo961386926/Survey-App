package com.qhiot.survey.service;

/**
 * 邮件发送服务
 * 作为短信下发失败或用户无手机号时的兜底通道
 */
public interface EmailService {

    /**
     * 发送密码重置邮件（HTML 格式）
     *
     * @param toEmail     收件邮箱
     * @param username    用户名
     * @param newPassword 新密码（明文）
     * @return 是否发送成功；失败时不抛异常，仅记录日志
     */
    boolean sendPasswordResetEmail(String toEmail, String username, String newPassword);
}
