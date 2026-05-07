package com.qhiot.survey.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应 DTO（包含角色ID列表）
 */
@Data
public class UserResponse {
    
    private Long id;
    
    private String username;
    
    private String realName;
    
    private String phone;
    
    private String email;
    
    /**
     * @deprecated 已废弃，请使用 roleIds
     */
    @Deprecated
    private Integer role;
    
    /**
     * 角色ID列表（支持多角色）
     */
    private List<Long> roleIds;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
