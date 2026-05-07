package com.qhiot.survey.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新用户请求 DTO
 */
@Data
public class UpdateUserRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long id;
    
    private String realName;
    
    private String password;
    
    private String phone;
    
    private String email;
    
    /**
     * 角色ID列表（支持多角色）
     */
    private List<Long> roleIds;
}
