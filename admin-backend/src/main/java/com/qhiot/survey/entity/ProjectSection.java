package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标段实体类
 */
@Data
@TableName("project_section")
public class ProjectSection implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long projectId;
    
    private String sectionName;
    
    private String sectionCode;
    
    private Long managerId;
    
    private String description;
    
    private Integer status;
    
    private Integer isKeyArea; // 是否重点区域 0-否 1-是
    
    /** 租户ID */
    private Long tenantId;

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
