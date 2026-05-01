package com.qhiot.survey.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 业务错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_LOCKED(1004, "用户已被锁定，请稍后再试"),
    TOKEN_EXPIRED(1005, "登录已过期，请重新登录"),
    TOKEN_INVALID(1006, "无效的Token"),
    
    PROJECT_NOT_FOUND(2001, "项目不存在"),
    POINT_NOT_FOUND(3001, "点位不存在"),
    TEMPLATE_NOT_FOUND(4001, "模板不存在");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}