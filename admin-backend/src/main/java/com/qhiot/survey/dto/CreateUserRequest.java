package com.qhiot.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 创建用户请求 DTO
 */
@Data
@Schema(description = "创建用户请求")
public class CreateUserRequest {

    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "Pass1234!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 角色ID列表（支持多角色）
     */
    @Schema(description = "角色ID列表（支持多角色）", example = "[1, 2]")
    private List<Long> roleIds;
}
