package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典项实体类（扩展版）
 */
@Data
@TableName("sys_dictionary_data")
public class SysDictItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 字典分类ID
     */
    @TableField("dict_id")
    private Long dictId;
    
    /**
     * 字典代码（冗余字段，便于查询）
     */
    @TableField("dict_code")
    private String dictCode;
    
    /**
     * 字典项名称
     */
    @TableField("data_name")
    private String itemLabel;
    
    /**
     * 字典项值
     */
    @TableField("data_value")
    private String itemValue;
    
    /**
     * 排序
     */
    @TableField("data_order")
    private Integer sortOrder;
    
    /**
     * 状态：0禁用 1启用
     */
    private Integer status;
    
    /**
     * 是否只读：0否 1是
     */
    @TableField("is_readonly")
    private Integer isReadonly;
    
    /**
     * 备注
     */
    private String remark;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
