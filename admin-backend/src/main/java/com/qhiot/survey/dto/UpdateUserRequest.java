package com.qhiot.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新用户请求 DTO
 */
@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "新密码（留空则不修改）", example = "NewPass123!")
    private String password;

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
