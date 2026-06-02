package com.qhiot.survey.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应 DTO（包含角色ID列表）
 */
@Data
@Schema(description = "用户响应（含角色和项目信息）")
public class UserResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 角色ID列表（支持多角色）
     */
    @Schema(description = "角色ID列表", example = "[1, 2]")
    private List<Long> roleIds;

    /**
     * 负责的项目名称（多个项目用逗号分隔）
     */
    @Schema(description = "负责的项目名称（多个项目用逗号分隔）", example = "排水管网项目,河道监测项目")
    private String projectName;

    @Schema(description = "状态：0禁用/1启用", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-06-01T12:00:00")
    private LocalDateTime updateTime;
}
