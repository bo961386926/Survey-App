package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目成员关联实体类
 */
@Data
@TableName("project_member")
public class ProjectMember implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色：admin-管理员, collector-采集员, auditor-审核员, viewer-查看者
     */
    private String role;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /** 租户ID */
    private Long tenantId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}