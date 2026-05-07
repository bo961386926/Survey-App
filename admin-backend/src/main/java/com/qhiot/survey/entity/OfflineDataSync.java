package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 离线数据同步记录实体类
 * 用于追踪移动端离线数据的同步状态
 */
@Data
@TableName("offline_data_sync")
public class OfflineDataSync implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 设备ID（移动端设备唯一标识）
     */
    private String deviceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 数据类型：survey_result-勘察结果, photo-照片, location-位置
     */
    private String dataType;

    /**
     * 数据ID（业务数据ID）
     */
    private String dataId;

    /**
     * 数据内容（JSON格式）
     */
    private String dataContent;

    /**
     * 同步状态：0待同步 1同步中 2已同步 3同步失败
     */
    private Integer syncStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 客户端创建时间（离线时的时间）
     */
    private LocalDateTime clientCreateTime;

    /**
     * 服务器接收时间
     */
    private LocalDateTime serverReceiveTime;

    /**
     * 同步完成时间
     */
    private LocalDateTime syncCompleteTime;

    /**
     * 版本号（用于冲突检测）
     */
    private Integer versionNo;

    /**
     * 冲突解决方案：server-以服务器为准, client-以客户端为准, merge-合并
     */
    private String conflictResolution;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
