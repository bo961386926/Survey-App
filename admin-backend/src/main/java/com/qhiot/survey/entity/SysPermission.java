package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体类
 */
@Data
@TableName("sys_permission")
public class SysPermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码，如 point:view
     */
    private String permCode;

    /**
     * 权限名称，如 查看点位
     */
    private String permName;

    /**
     * 所属模块，如 point/audit/user
     */
    private String module;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 1启用 0禁用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
