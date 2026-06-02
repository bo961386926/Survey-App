package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 勘察指派任务实体类
 */
@Data
@TableName("sys_task")
public class SysTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String taskName;

    private Long projectId;

    private String plotCode;

    private String precisionRequirement;

    private String sensorType;

    /**
     * 优先级：0普通 1重要 2紧急
     */
    private Integer priority;

    /**
     * 任务目标/描述
     */
    private String description;

    /**
     * 状态：0待分配 1进行中 2已完成 3已逾期 4已终止
     */
    private Integer status;

    /**
     * 截止日期
     */
    private LocalDateTime deadline;

    /**
     * 指派执行人ID
     */
    private Long assigneeId;

    /**
     * 创建人/负责人 ID
     */
    private Long ownerUserId;

    private String category;

    /**
     * 子任务清单，存储JSON数组的字符串
     */
    private String subtasks;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String projectName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String assigneeName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
