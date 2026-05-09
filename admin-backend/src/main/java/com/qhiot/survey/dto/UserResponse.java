package com.qhiot.survey.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应 DTO（包含角色ID列表）
 */
@Data
public class UserResponse {
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    private String username;
    
    private String realName;
    
    private String phone;
    
    private String email;
    
    /**
     * 角色ID列表（支持多角色）
     */
    private List<Long> roleIds;
    
    /**
     * 负责的项目名称（多个项目用逗号分隔）
     */
    private String projectName;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
