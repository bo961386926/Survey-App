package com.qhiot.survey.service;

import com.qhiot.survey.entity.SysUser;

/**
 * 密码下发通知服务
 *
 * <p>主通道：短信；兜底通道：邮件。
 * 任一通道成功即视为通知成功；两个通道均失败时仅记录日志，不影响主业务。</p>
 */
public interface PasswordNotificationService {

    /**
     * 异步下发新密码到目标用户。
     *
     * @param user        目标用户
     * @param newPassword 重置后的明文密码
     */
    void notifyPasswordReset(SysUser user, String newPassword);
}
