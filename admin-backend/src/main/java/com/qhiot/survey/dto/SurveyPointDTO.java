package com.qhiot.survey.dto;

import com.qhiot.survey.entity.SurveyPoint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 点位信息DTO（包含项目名称）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SurveyPointDTO extends SurveyPoint {
    
    /**
     * 项目名称
     */
    private String projectName;
    
    /**
     * 项目编号
     */
    private String projectCode;
    
    /**
     * 标段名称
     */
    private String sectionName;
    
    /**
     * 采集人姓名
     */
    private String collectorName;
    
    /**
     * 分配人姓名
     */
    private String assigneeName;
}
