package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 位置纠偏日志实体类
 */
@Data
@TableName("location_correction_log")
public class LocationCorrectionLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long pointId;
    
    private Long resultId;
    
    private BigDecimal originalLng;
    
    private BigDecimal originalLat;
    
    private BigDecimal correctedLng;
    
    private BigDecimal correctedLat;
    
    private Long userId;
    
    private LocalDateTime createTime;
}