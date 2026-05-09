package com.qhiot.survey.common.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限码注册中心
 * 启动时扫描所有 @PreAuthorize 注解，注册所有权限码
 * 用于 admin 通配符展开和权限管理
 */
@Slf4j
@Component
public class PermissionRegistry implements BeanPostProcessor {

    /**
     * 所有已注册的权限码（不含 ROLE_ 前缀）
     */
    private static final Set<String> ALL_PERMISSIONS = ConcurrentHashMap.newKeySet();

    /**
     * 通配符标记 - 拥有此权限的用户视为拥有所有权限
     */
    public static final String WILDCARD = "*";

    /**
     * 获取所有已注册的权限码
     */
    public static Set<String> getAllPermissions() {
        return Collections.unmodifiableSet(ALL_PERMISSIONS);
    }

    /**
     * 判断权限列表是否包含通配符
     */
    public static boolean hasWildcard(Set<String> permissions) {
        return permissions != null && permissions.contains(WILDCARD);
    }

    /**
     * 展开通配符：如果包含 "*" 则返回所有已注册权限码
     */
    public static Set<String> expandWildcard(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new HashSet<>();
        }
        if (permissions.contains(WILDCARD)) {
            Set<String> expanded = new HashSet<>(ALL_PERMISSIONS);
            // 保留原始的 ROLE_ 权限
            permissions.stream()
                    .filter(p -> p.startsWith("ROLE_"))
                    .forEach(expanded::add);
            return expanded;
        }
        return new HashSet<>(permissions);
    }

    /**
     * 展开通配符（数组版本）
     */
    public static String[] expandWildcard(String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return new String[0];
        }
        Set<String> permSet = new HashSet<>(Arrays.asList(permissions));
        if (permSet.contains(WILDCARD)) {
            Set<String> expanded = new HashSet<>(ALL_PERMISSIONS);
            // 保留 ROLE_ 权限
            permSet.stream()
                    .filter(p -> p.startsWith("ROLE_"))
                    .forEach(expanded::add);
            return expanded.toArray(new String[0]);
        }
        return permissions;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        // 只扫描 @RestController 的 bean
        if (AnnotationUtils.findAnnotation(targetClass, RestController.class) == null) {
            return bean;
        }

        for (Method method : targetClass.getDeclaredMethods()) {
            PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
            if (preAuthorize != null) {
                String value = preAuthorize.value();
                if (StringUtils.hasText(value)) {
                    // 提取 hasAuthority 或 hasPermission 中的权限码
                    parseAndRegister(value);
                }
            }
        }
        return bean;
    }

    /**
     * 解析 @PreAuthorize 表达式，提取权限码
     * 支持格式：
     *   hasAuthority('point:view')
     *   hasAnyAuthority('point:view', 'point:edit')
     *   hasRole('ADMIN') → 跳过，这是角色检查
     */
    private void parseAndRegister(String expression) {
        if (expression == null || expression.isEmpty()) return;

        // 提取 hasAuthority 和 hasAnyAuthority 中的权限码
        extractAuthorityValues(expression, "hasAuthority(");
        extractAuthorityValues(expression, "hasAnyAuthority(");
    }

    private void extractAuthorityValues(String expression, String prefix) {
        int idx = expression.indexOf(prefix);
        while (idx >= 0) {
            int start = idx + prefix.length();
            int end = findClosingParen(expression, start);
            if (end > start) {
                String content = expression.substring(start, end);
                // 分割逗号，去掉引号和空格
                String[] parts = content.split(",");
                for (String part : parts) {
                    String perm = part.trim()
                            .replace("'", "")
                            .replace("\"", "")
                            .trim();
                    if (!perm.isEmpty() && !perm.startsWith("ROLE_") && !perm.equals(WILDCARD)) {
                        ALL_PERMISSIONS.add(perm);
                        log.debug("注册权限码: {}", perm);
                    }
                }
            }
            idx = expression.indexOf(prefix, end + 1);
        }
    }

    private int findClosingParen(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '(') depth++;
            else if (s.charAt(i) == ')') {
                if (depth == 0) return i;
                depth--;
            }
        }
        return -1;
    }

    @PostConstruct
    public void logSummary() {
        log.info("====== 权限码注册完成，共 {} 个 ======", ALL_PERMISSIONS.size());
        if (!ALL_PERMISSIONS.isEmpty()) {
            log.info("已注册权限码: {}", String.join(", ", ALL_PERMISSIONS));
        }
    }
}
