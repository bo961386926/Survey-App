package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典项实体类
 * 表名: sys_dictionary_data
 */
@Data
@TableName("sys_dictionary_data")
public class SysDictionaryData implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 字典项ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典分类ID */
    private Long dictId;

    /** 字典代码（冗余字段，便于查询） */
    private String dictCode;

    /** 字典项名称 */
    private String dataName;

    /** 字典项值 */
    private String dataValue;

    /** 排序 */
    private Integer dataOrder;

    /** 状态：0禁用 1启用 */
    private Integer status;

    /** 是否只读：0否 1是 */
    private Integer isReadonly;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}