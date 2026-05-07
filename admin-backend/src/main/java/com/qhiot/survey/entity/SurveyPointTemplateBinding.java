package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点位模板绑定实体类
 */
@Data
@TableName("survey_point_template_binding")
public class SurveyPointTemplateBinding implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long projectId;
    
    private Long sectionId;
    
    private String outfallType;
    
    private Long templateId;
    
    private Long templateVersionId;
    
    private LocalDateTime createTime;
}