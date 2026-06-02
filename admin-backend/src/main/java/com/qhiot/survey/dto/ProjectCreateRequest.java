package com.qhiot.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 项目创建请求DTO
 */
@Data
@Schema(description = "项目创建/更新请求")
public class ProjectCreateRequest {

    @Schema(description = "项目名称", example = "青泓市排水管网勘察项目", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectName;

    @Schema(description = "项目编号", example = "QH-2024-001")
    private String projectCode;

    @Schema(description = "项目负责人", example = "李工")
    private String manager;

    @Schema(description = "所在区域", example = "浙江省杭州市")
    private String region;

    @Schema(description = "客户名称", example = "杭州市水务集团")
    private String clientName;

    @Schema(description = "项目描述", example = "杭州市主城区排水管网勘察项目")
    private String description;

    @Schema(description = "开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-12-31")
    private LocalDate endDate;
}
