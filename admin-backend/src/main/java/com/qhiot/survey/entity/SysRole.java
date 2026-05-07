package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String roleCode;
    
    private String roleName;
    
    /**
     * 权限列表，JSON数组
     */
    private String permissions;
    
    private Integer status;
    
    /**
     * 排序
     */
    private Integer sort;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}