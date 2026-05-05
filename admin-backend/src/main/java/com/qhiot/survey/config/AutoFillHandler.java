package com.qhiot.survey.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.qhiot.survey.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 自动填充审计字段：create_time, update_time, create_by, update_by
 */
@Slf4j
@Component
public class AutoFillHandler implements MetaObjectHandler {
    
    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        
        // 创建人
        try {
            String username = SecurityUtils.getCurrentUsername();
            this.strictInsertFill(metaObject, "createBy", () -> username, String.class);
        } catch (Exception e) {
            log.warn("获取当前用户失败，跳过createBy填充");
        }
    }
    
    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        
        // 更新人
        try {
            String username = SecurityUtils.getCurrentUsername();
            this.strictUpdateFill(metaObject, "updateBy", () -> username, String.class);
        } catch (Exception e) {
            log.warn("获取当前用户失败，跳过updateBy填充");
        }
    }
}
