package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体类
 */
@Data
@TableName("project")
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    private String projectName;
    
    private String projectCode;
    
    private String manager;
    
    /**
     * 所属区域
     */
    private String region;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 状态：0草稿 1进行中 2已暂停 3已完成 4已归档
     */
    private Integer status;

    /**
     * 委托单位/客户名称
     */
    private String clientName;

    /**
     * 项目描述/备注
     */
    private String description;

    /**
     * 绑定模板数
     */
    private Integer templateCount;
    
    /**
     * 点位总数
     */
    private Integer pointCount;
    
    /**
     * 已完成数量
     */
    private Integer completedCount;
    
    /**
     * 待审核数量
     */
    private Integer pendingAuditCount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}