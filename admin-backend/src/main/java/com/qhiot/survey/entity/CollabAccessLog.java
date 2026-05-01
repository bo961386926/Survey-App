package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协作访问日志实体类
 */
@Data
@TableName("collab_access_log")
@Schema(description = "协作访问日志")
public class CollabAccessLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "协作入口ID")
    private Long entryId;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "访问IP")
    private String ip;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "请求路径")
    private String requestPath;

    @Schema(description = "响应状态码")
    private Integer responseCode;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}