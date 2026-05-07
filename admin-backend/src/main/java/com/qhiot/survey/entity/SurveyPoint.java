package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 勘查点位实体类
 */
@Data
@TableName("survey_point")
public class SurveyPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 点位编号
     */
    private String pointCode;
    
    private String pointName;
    
    private Long projectId;
    
    /**
     * 标段ID
     */
    private Long sectionId;
    
    /**
     * 排口类型(雨水排口/污水排口/雨污混流口等)
     */
    private String outfallType;
    
    private BigDecimal longitude;
    
    private BigDecimal latitude;
    
    /**
     * 行政区
     */
    private String region;
    
    /**
     * 分配人ID
     */
    private Long assigneeId;
    
    /**
     * 采集人ID
     */
    private Long collectorId;
    
    /**
     * 状态：0待采集 1草稿中 2待审核 3审核通过 4驳回待修改 5已归档 6作废
     */
    private Integer status;
    
    /**
     * 异常标签
     */
    private String abnormalTag;
    
    /**
     * 1已删除 0未删除
     */
    @TableLogic
    private Integer isDeleted;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}