package com.qhiot.survey.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 项目创建请求DTO
 */
@Data
public class ProjectCreateRequest {
    private String projectName;
    private String projectCode;
    private String manager;
    private String region;
    private LocalDate startDate;
    private LocalDate endDate;
}