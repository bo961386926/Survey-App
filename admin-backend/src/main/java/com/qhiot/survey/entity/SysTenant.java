package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户实体
 */
@Data
@TableName("sys_tenant")
public class SysTenant implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 租户编码（唯一，用于登录识别） */
    private String tenantCode;

    /** 租户名称 */
    private String tenantName;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 1启用 0禁用 */
    private Integer status;

    /** 租期到期时间 */
    private LocalDateTime expireTime;

    /** 最大用户数 */
    private Integer maxUserCount;

    /** 最大项目数 */
    private Integer maxProjectCount;

    /** 租户级扩展配置 */
    private String configJson;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
