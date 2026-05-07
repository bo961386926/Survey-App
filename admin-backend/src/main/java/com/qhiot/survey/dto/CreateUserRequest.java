package com.qhiot.survey.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 创建用户请求 DTO
 */
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String realName;
    
    private String phone;
    
    private String email;
    
    /**
     * 角色ID列表（支持多角色）
     */
    private List<Long> roleIds;
}
