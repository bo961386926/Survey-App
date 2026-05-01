package com.qhiot.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 协作入口实体类
 */
@Data
@TableName("collab_entry")
public class CollabEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String entryName;
    
    /**
     * 协作访问令牌
     */
    private String token;
    
    /**
     * 授权项目ID列表，JSON数组
     */
    private String projectIds;
    
    /**
     * 授权点位ID列表，JSON数组
     */
    private String pointIds;
    
    /**
     * 权限范围配置
     */
    private String permissions;
    
    private LocalDateTime expireTime;
    
    /**
     * 状态：0未启用 1启用中 2已过期 3已撤销
     */
    private Integer status;
    
    private Long creatorId;
    
    private Integer accessCount;
    
    private LocalDateTime lastAccessTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}