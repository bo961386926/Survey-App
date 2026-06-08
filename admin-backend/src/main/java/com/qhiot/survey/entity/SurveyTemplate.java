package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 勘查模板主表实体类
 */
@Data
@TableName("survey_template")
public class SurveyTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String templateName;
    
    /**
     * 模板编码
     */
    private String templateCode;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 状态：0草稿 1已发布 2已停用
     */
    private Integer status;
    
    /**
     * 字段配置JSON（兼容旧版本）
     */
    private String fieldsJson;
    
    /**
     * 当前版本ID
     */
    private Long currentVersionId;
    
    /**
     * 排口类型（关联数据字典）
     */
    private String outletType;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /** 租户ID */
    private Long tenantId;

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}