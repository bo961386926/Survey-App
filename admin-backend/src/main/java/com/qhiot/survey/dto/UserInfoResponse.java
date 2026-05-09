package com.qhiot.survey.dto;

import com.qhiot.survey.common.util.PermissionRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户信息响应
 */
@Data
@AllArgsConstructor
public class UserInfoResponse {
    private String userId;
    private String userName;
    private String realName;
    private String[] roles;
    /** 权限码列表（通配符已展开） */
    private String[] permissions;
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
