package com.qhiot.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * 管理员重置用户密码请求DTO
 */
@Data
@Schema(description = "管理员重置用户密码请求")
public class ResetPasswordRequest {

    @Schema(description = "新密码（8-64位，需包含大小写字母、数字和特殊字符）", example = "NewPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在8-64位之间")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
        message = "密码必须包含大小写字母、数字和特殊字符"
    )
    private String newPassword;
}
