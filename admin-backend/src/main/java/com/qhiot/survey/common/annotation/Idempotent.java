package com.qhiot.survey.common.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性注解
 * 用于防止重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * Token有效期（秒）
     * 默认5分钟
     */
    long expireSeconds() default 300;
    
    /**
     * 错误提示消息
     */
    String message() default "请勿重复提交";
}
