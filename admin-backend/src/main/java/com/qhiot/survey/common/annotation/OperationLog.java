package com.qhiot.survey.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作模块
     */
    String module();
    
    /**
     * 操作类型
     */
    String action();
    
    /**
     * 操作描述（支持SpEL表达式）
     * 例如: "创建用户: #user.username"
     */
    String description() default "";
    
    /**
     * 目标类型（用于关联具体业务对象）
     */
    String targetType() default "";
    
    /**
     * 风险等级：0低 1中 2高
     */
    int riskLevel() default 0;
}
