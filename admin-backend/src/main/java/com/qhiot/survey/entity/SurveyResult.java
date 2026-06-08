package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 勘查结果实体类
 */
@Data
@TableName("survey_result")
public class SurveyResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long pointId;
    
    /**
     * 版本号，按点位自增，从1开始
     */
    private Integer versionNo;
    
    /**
     * 使用的模板版本ID
     */
    private Long templateVersionId;
    
    /**
     * 业务表单数据，JSON格式
     */
    private String formData;
    
    /**
     * 照片列表URL，JSON数组
     */
    private String images;
    
    /**
     * 结果状态：0草稿 1已提交 2待审核 3已通过 4已驳回 5已归档
     */
    private Integer resultStatus;
    
    /**
     * 审核状态：0待审 1通过 2驳回(冗余字段，便于查询)
     */
    private Integer auditStatus;
    
    /**
     * 驳回意见
     */
    private String auditRemark;
    
    /**
     * 勘查人员ID
     */
    private Long surveyUserId;
    
    /**
     * 乐观锁版本号
     */
    private Integer optimisticLockVersion;
    
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核人ID
     */
    private Long auditorId;
    
    /**
     * 1已删除 0未删除
     */
    @TableLogic
    private Integer isDeleted;
    
    /** 租户ID */
    private Long tenantId;

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}