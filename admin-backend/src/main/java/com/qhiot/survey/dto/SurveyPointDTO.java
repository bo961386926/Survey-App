package com.qhiot.survey.dto;

import com.qhiot.survey.entity.SurveyPoint;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 点位信息DTO（包含项目名称）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "点位信息DTO（含关联项目名称等扩展字段）")
public class SurveyPointDTO extends SurveyPoint {

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", example = "青泓市排水管网勘察项目")
    private String projectName;

    /**
     * 项目编号
     */
    @Schema(description = "项目编号", example = "QH-2024-001")
    private String projectCode;

    /**
     * 标段名称
     */
    @Schema(description = "标段名称", example = "一标段")
    private String sectionName;

    /**
     * 采集人姓名
     */
    @Schema(description = "采集人姓名", example = "张三")
    private String collectorName;

    /**
     * 分配人姓名
     */
    @Schema(description = "分配人姓名", example = "李工")
    private String assigneeName;
}
