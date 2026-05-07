package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息中心实体类
 */
@Data
@TableName("message_center")
public class MessageCenter implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    /**
     * 消息类型：audit_reminder/project_delay/template_publish/export_complete/collab_expire/risk_alert
     */
    private String msgType;
    
    private String msgTitle;
    
    private String msgContent;
    
    private String targetType;
    
    private Long targetId;
    
    /**
     * 0未读 1已读
     */
    private Integer isRead;
    
    private LocalDateTime readTime;
    
    /**
     * 推送状态：0未推送 1已推送 2推送失败
     */
    private Integer pushStatus;
    
    private LocalDateTime createTime;
}