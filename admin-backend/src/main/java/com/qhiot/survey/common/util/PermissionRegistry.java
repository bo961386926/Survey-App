package com.qhiot.survey.common.util;

import com.qhiot.survey.common.constant.Permissions;
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
import java.util.Map;
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
     * 旧版权限码到新版领域权限码的兼容映射。
     *
     * 本地/线上已有角色数据不会因为初始化 SQL 更新而自动迁移，登录时统一归一化，避免旧权限码导致接口 403。
     */
    private static final Map<String, Set<String>> LEGACY_PERMISSION_ALIASES = Map.ofEntries(
            Map.entry("user:list", Set.of(Permissions.SYSTEM_USER)),
            Map.entry("user:create", Set.of(Permissions.SYSTEM_USER)),
            Map.entry("user:edit", Set.of(Permissions.SYSTEM_USER)),
            Map.entry("user:delete", Set.of(Permissions.SYSTEM_USER)),
            Map.entry("user:reset-password", Set.of(Permissions.SYSTEM_USER)),
            Map.entry("user:export", Set.of(Permissions.SYSTEM_USER)),
            Map.entry("role:list", Set.of(Permissions.SYSTEM_ROLE)),
            Map.entry("role:create", Set.of(Permissions.SYSTEM_ROLE)),
            Map.entry("role:edit", Set.of(Permissions.SYSTEM_ROLE)),
            Map.entry("role:delete", Set.of(Permissions.SYSTEM_ROLE)),
            Map.entry("role:permission", Set.of(Permissions.SYSTEM_ROLE)),
            Map.entry("dict:list", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dict:create", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dict:edit", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dict:delete", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary:list", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary:create", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary:edit", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary:delete", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary-data:list", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary-data:create", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary-data:edit", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("dictionary-data:delete", Set.of(Permissions.SYSTEM_DICT)),
            Map.entry("log:list", Set.of(Permissions.SYSTEM_LOG)),
            Map.entry("operation-log:list", Set.of(Permissions.SYSTEM_LOG)),
            Map.entry("login-log:list", Set.of(Permissions.SYSTEM_LOG)),
            Map.entry("project:list", Set.of(Permissions.PROJECT_VIEW)),
            Map.entry("project:detail", Set.of(Permissions.PROJECT_VIEW)),
            Map.entry("project:create", Set.of(Permissions.PROJECT_EDIT)),
            Map.entry("project:update", Set.of(Permissions.PROJECT_EDIT)),
            Map.entry("project:delete", Set.of(Permissions.PROJECT_EDIT)),
            Map.entry("project:archive", Set.of(Permissions.PROJECT_EDIT)),
            Map.entry("project:restore", Set.of(Permissions.PROJECT_EDIT)),
            Map.entry("project:status", Set.of(Permissions.PROJECT_EDIT)),
            Map.entry("point:list", Set.of(Permissions.POINT_VIEW)),
            Map.entry("point:detail", Set.of(Permissions.POINT_VIEW)),
            Map.entry("point:history", Set.of(Permissions.POINT_VIEW)),
            Map.entry("point:create", Set.of(Permissions.POINT_EDIT)),
            Map.entry("point:update", Set.of(Permissions.POINT_EDIT)),
            Map.entry("point:delete", Set.of(Permissions.POINT_EDIT)),
            Map.entry("point:import", Set.of(Permissions.POINT_EDIT)),
            Map.entry("point:assign", Set.of(Permissions.POINT_EDIT)),
            Map.entry("point:invalidate", Set.of(Permissions.POINT_EDIT)),
            Map.entry("template:list", Set.of(Permissions.TEMPLATE_VIEW)),
            Map.entry("template:detail", Set.of(Permissions.TEMPLATE_VIEW)),
            Map.entry("template:create", Set.of(Permissions.TEMPLATE_EDIT)),
            Map.entry("template:update", Set.of(Permissions.TEMPLATE_EDIT)),
            Map.entry("template:delete", Set.of(Permissions.TEMPLATE_EDIT)),
            Map.entry("template:publish", Set.of(Permissions.TEMPLATE_EDIT)),
            Map.entry("template:draft", Set.of(Permissions.TEMPLATE_EDIT)),
            Map.entry("template:bind", Set.of(Permissions.TEMPLATE_BIND)),
            Map.entry("survey:list", Set.of(Permissions.SURVEY_EDIT, Permissions.AUDIT_VIEW)),
            Map.entry("survey:detail", Set.of(Permissions.SURVEY_EDIT, Permissions.AUDIT_VIEW)),
            Map.entry("survey:create", Set.of(Permissions.SURVEY_CREATE)),
            Map.entry("survey:edit", Set.of(Permissions.SURVEY_EDIT)),
            Map.entry("survey:submit", Set.of(Permissions.SURVEY_SUBMIT)),
            Map.entry("result:list", Set.of(Permissions.SURVEY_EDIT, Permissions.AUDIT_VIEW)),
            Map.entry("result:detail", Set.of(Permissions.SURVEY_EDIT, Permissions.AUDIT_VIEW)),
            Map.entry("result:latest", Set.of(Permissions.SURVEY_EDIT, Permissions.AUDIT_VIEW)),
            Map.entry("result:create", Set.of(Permissions.SURVEY_CREATE)),
            Map.entry("result:update", Set.of(Permissions.SURVEY_EDIT)),
            Map.entry("result:delete", Set.of(Permissions.SURVEY_EDIT)),
            Map.entry("result:submit", Set.of(Permissions.SURVEY_SUBMIT)),
            Map.entry("audit:list", Set.of(Permissions.AUDIT_VIEW)),
            Map.entry("audit:detail", Set.of(Permissions.AUDIT_VIEW)),
            Map.entry("audit:pass", Set.of(Permissions.AUDIT_PASS)),
            Map.entry("audit:batch-pass", Set.of(Permissions.AUDIT_PASS)),
            Map.entry("audit:reject", Set.of(Permissions.AUDIT_REJECT)),
            Map.entry("task:list", Set.of(Permissions.TASK_VIEW)),
            Map.entry("task:detail", Set.of(Permissions.TASK_VIEW)),
            Map.entry("task:create", Set.of(Permissions.TASK_EDIT)),
            Map.entry("task:update", Set.of(Permissions.TASK_EDIT)),
            Map.entry("task:delete", Set.of(Permissions.TASK_EDIT)),
            Map.entry("task:status", Set.of(Permissions.TASK_EDIT)),
            Map.entry("task:assign", Set.of(Permissions.TASK_ASSIGN)),
            Map.entry("export:list", Set.of(Permissions.EXPORT_PROJECT, Permissions.EXPORT_AUDIT)),
            Map.entry("export:download", Set.of(Permissions.EXPORT_PROJECT, Permissions.EXPORT_AUDIT)),
            Map.entry("message:list", Set.of(Permissions.MESSAGE_PUSH)),
            Map.entry("message:push", Set.of(Permissions.MESSAGE_PUSH))
    );

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
     * 展开通配符并兼容旧版权限码：如果包含 "*" 则返回所有已注册权限码
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
        return normalizeLegacyPermissions(permissions);
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
        return normalizeLegacyPermissions(permSet).toArray(new String[0]);
    }

    private static Set<String> normalizeLegacyPermissions(Set<String> permissions) {
        Set<String> normalized = new HashSet<>();
        for (String permission : permissions) {
            if (!StringUtils.hasText(permission)) {
                continue;
            }
            String trimmed = permission.trim();
            normalized.add(trimmed);
            Set<String> aliases = LEGACY_PERMISSION_ALIASES.get(trimmed);
            if (aliases != null) {
                normalized.addAll(aliases);
            }
        }
        return normalized;
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
        ALL_PERMISSIONS.addAll(Arrays.asList(Permissions.getAll()));
        log.info("====== 权限码注册完成，共 {} 个 ======", ALL_PERMISSIONS.size());
        if (!ALL_PERMISSIONS.isEmpty()) {
            log.info("已注册权限码: {}", String.join(", ", ALL_PERMISSIONS));
        }
    }
}
