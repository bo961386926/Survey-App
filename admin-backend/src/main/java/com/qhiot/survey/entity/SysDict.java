package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典实体类（扩展版）
 */
@Data
@TableName("sys_dictionary")
public class SysDict implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 字典代码（唯一标识）
     */
    private String dictCode;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 字典描述
     */
    private String description;
    
    /**
     * 是否系统内置：0否 1是
     */
    @TableField("is_system")
    private Integer isSystem;
    
    /**
     * 状态：0禁用 1启用
     */
    private Integer status;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
