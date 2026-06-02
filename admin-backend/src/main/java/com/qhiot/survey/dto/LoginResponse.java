package com.qhiot.survey.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌（Access Token）", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌（Refresh Token）", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "管理员")
    private String realName;

    /**
     * 用户角色编码列表（从 sys_role 表获取，如 ["admin", "auditor"]）
     */
    @Schema(description = "用户角色编码列表", example = "[\"admin\", \"auditor\"]")
    private String[] roleCodes;

    /**
     * 用户权限列表（从角色的 permissions 字段聚合，如 ["user:list", "user:create"]）
     */
    @Schema(description = "用户权限列表（通配符已展开）", example = "[\"user:list\", \"user:create\"]")
    private String[] permissions;

    @Schema(description = "是否首次登录（需修改密码）", example = "false")
    private Boolean isFirstLogin;

    /**
     * 登录警告信息（如：登录失败次数过多、异地登录等）
     */
    @Schema(description = "登录警告信息（如登录失败次数过多、异地登录等）", example = "null")
    private String loginWarning;
}
