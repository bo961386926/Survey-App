package com.qhiot.survey.dto;

import com.qhiot.survey.common.util.PermissionRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户信息响应
 */
@Data
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserInfoResponse {

    @Schema(description = "用户ID", example = "1")
    private String userId;

    @Schema(description = "用户名", example = "admin")
    private String userName;

    @Schema(description = "真实姓名", example = "管理员")
    private String realName;

    @Schema(description = "角色编码列表", example = "[\"admin\"]")
    private String[] roles;

    /** 权限码列表（通配符已展开） */
    @Schema(description = "权限码列表（通配符已展开）", example = "[\"user:list\", \"user:create\"]")
    private String[] permissions;

    @Schema(description = "按钮权限列表", example = "[]")
    private String[] buttons;

    /**
     * 带权限的构造方法（自动展开通配符）
     */
    public static UserInfoResponse create(String userId, String userName, String realName,
                                           String[] roles, String[] permissions, String[] buttons) {
        // 展开通配符
        String[] expandedPerms = PermissionRegistry.expandWildcard(permissions);
        return new UserInfoResponse(userId, userName, realName, roles, expandedPerms, buttons);
    }
}
