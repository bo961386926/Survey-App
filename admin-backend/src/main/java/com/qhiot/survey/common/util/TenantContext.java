package com.qhiot.survey.common.util;

/**
 * 租户上下文（请求级 ThreadLocal）
 *
 * 在 JwtAuthFilter 中解析 token 后写入，在请求结束时清理。
 * MyBatis Plus TenantLineInterceptor 自动读取此值追加到 SQL。
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();

    /** 设置当前请求的租户ID */
    public static void setTenantId(Long tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    /** 获取当前请求的租户ID，null 表示不隔离（系统管理员） */
    public static Long getTenantId() {
        return TENANT_HOLDER.get();
    }

    /** 请求结束时清理 */
    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
