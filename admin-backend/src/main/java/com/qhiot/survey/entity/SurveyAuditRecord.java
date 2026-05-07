package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录实体类
 */
@Data
@TableName("survey_audit_record")
public class SurveyAuditRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long resultId;
    
    private Long pointId;
    
    private Long auditorId;
    
    /**
     * 操作：pass/reject/transfer(预留)
     */
    private String action;
    
    private String auditComment;
    
    private Long rejectTemplateId;
    
    private LocalDateTime createTime;
}