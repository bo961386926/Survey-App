package com.qhiot.survey.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private String realName;
    /**
     * 用户角色编码列表（从 sys_role 表获取，如 ["admin", "auditor"]）
     */
    private String[] roleCodes;
    /**
     * 用户权限列表（从角色的 permissions 字段聚合，如 ["user:list", "user:create"]）
     */
    private String[] permissions;
    private Boolean isFirstLogin;
    /**
     * 登录警告信息（如：登录失败次数过多、异地登录等）
     */
    private String loginWarning;
}
