package com.qhiot.survey.dto;

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
    private String[] buttons;
}
