package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String module;
    
    private String action;
    
    private String targetType;
    
    private Long targetId;
    
    private String detail;
    
    private String ip;
    
    private String userAgent;
    
    /**
     * 风险等级：0低 1中 2高
     */
    private Integer riskLevel;
    
    private LocalDateTime createTime;
}