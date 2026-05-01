package com.qhiot.survey.dto;

import lombok.Data;

/**
 * 项目查询请求DTO
 */
@Data
public class ProjectQueryRequest {
    private String projectName;
    private String projectCode;
    private String manager;
    private String region;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}