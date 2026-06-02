package com.qhiot.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目查询请求DTO
 */
@Data
@Schema(description = "项目查询请求")
public class ProjectQueryRequest {

    @Schema(description = "项目名称（模糊搜索）", example = "排水")
    private String projectName;

    @Schema(description = "项目编号", example = "QH-2024-001")
    private String projectCode;

    @Schema(description = "项目负责人", example = "李工")
    private String manager;

    @Schema(description = "所在区域", example = "杭州")
    private String region;

    @Schema(description = "项目状态：0草稿/1进行中/2已暂停/3已完成/4已归档", example = "1")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;
}
