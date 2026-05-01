package com.qhiot.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
    private Boolean isFirstLogin;
    /**
     * 登录警告信息（如：登录失败次数过多、异地登录等）
     */
    private String loginWarning;
}
