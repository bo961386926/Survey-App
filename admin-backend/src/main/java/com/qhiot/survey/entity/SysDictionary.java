package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典分类实体类
 * 表名: sys_dictionary
 */
@Data
@TableName("sys_dictionary")
public class SysDictionary implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 字典ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 字典代码（唯一标识） */
    private String dictCode;

    /** 字典名称 */
    private String dictName;

    /** 字典描述 */
    private String description;

    /** 是否系统内置：0否 1是 */
    private Integer isSystem;

    /** 状态：0禁用 1启用 */
    private Integer status;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}