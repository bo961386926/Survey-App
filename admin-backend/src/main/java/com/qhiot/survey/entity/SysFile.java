package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件存储实体类
 */
@Data
@TableName("sys_file")
@Schema(description = "文件信息")
public class SysFile {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "业务类型：survey_photo/template_image")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "上传人ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}