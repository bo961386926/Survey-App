package com.qhiot.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
@Schema(description = "账号密码登录请求")
public class LoginRequest {

    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "密码", example = "Admin123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "图形验证码", example = "1234")
    private String captcha;

    @Schema(description = "验证码Key（获取验证码时返回）", example = "a1b2c3d4e5f6")
    private String captchaKey;
}
