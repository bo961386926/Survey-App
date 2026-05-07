package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模板版本实体类
 */
@Data
@TableName("survey_template_version")
public class SurveyTemplateVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long templateId;
    
    private Integer versionNo;
    
    private String fieldsJson;
    
    private String rulesJson;
    
    private String linkageRulesJson;
    
    private Integer status;
    
    private LocalDateTime publishTime;
    
    private Long creatorId;
    
    private LocalDateTime createTime;
}