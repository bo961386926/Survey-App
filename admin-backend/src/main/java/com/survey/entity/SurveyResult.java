package com.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("survey_result")
public class SurveyResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long pointId;
    private String formData;
    private String images;
    private Integer version;
    private Integer isLatest;
    private Integer auditStatus;
    private String auditRemark;
    private String surveyUser;
    private LocalDateTime createTime;
}
