package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@TableName("announcement")
public class Announcement implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告类型：work_spec/maintenance_reminder/system_notification */
    private String type;

    /** 公告内容 */
    private String content;

    /** 发布人ID */
    private Long publisherId;

    /** 状态：0草稿 1定时发布 2已发布 3已撤回 */
    private Integer status;

    /** 定时发布时间 */
    private LocalDateTime publishTime;

    /** 受众范围 */
    private String targetScope;

    /** 租户ID */
    private Long tenantId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
