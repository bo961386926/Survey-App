package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导出任务实体类
 */
@Data
@TableName("export_task")
public class ExportTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String taskName;
    
    /**
     * 任务类型：point_list/audit_result/pdf_single/pdf_batch
     */
    private String taskType;
    
    private Long projectId;
    
    /**
     * 点位ID（用于PDF导出）
     */
    private Long pointId;
    
    /**
     * 勘察结果ID（用于PDF导出）
     */
    private Long resultId;
    
    /**
     * 状态：0待生成 1生成中 2已完成 3失败 4已过期
     */
    private Integer status;
    
    private String fileUrl;
    
    /**
     * 磁盘文件名（存储在导出目录下的实际文件名）
     */
    private String fileName;
    
    private Long fileSize;
    
    private LocalDateTime expireTime;
    
    private String errorMsg;
    
    private Long creatorId;
    
    /** 租户ID */
    private Long tenantId;

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}