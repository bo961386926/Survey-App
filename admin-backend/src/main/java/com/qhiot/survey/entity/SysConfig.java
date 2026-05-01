package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
@TableName("sys_config")
@Schema(description = "系统配置")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型：string/json/number")
    private String configType;

    @Schema(description = "配置说明")
    private String description;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}