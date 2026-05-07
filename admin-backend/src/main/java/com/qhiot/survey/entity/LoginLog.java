package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@TableName("login_log")
public class LoginLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    /**
     * 登录类型：internal/collab
     */
    private String loginType;
    
    /**
     * 状态：0成功 1失败
     */
    private Integer status;
    
    private String failReason;
    
    private String ip;
    
    private String userAgent;
    
    private LocalDateTime createTime;
}