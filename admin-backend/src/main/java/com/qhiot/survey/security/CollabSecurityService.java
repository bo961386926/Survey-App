package com.qhiot.survey.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.common.util.IpUtils;
import com.qhiot.survey.entity.CollabAccessLog;
import com.qhiot.survey.entity.CollabEntry;
import com.qhiot.survey.mapper.CollabAccessLogMapper;
import com.qhiot.survey.mapper.CollabEntryMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 协作令牌安全服务：
 * <ul>
 *     <li>校验协作入口（状态、有效期）</li>
 *     <li>白名单方式判定协作令牌是否可访问当前请求</li>
 *     <li>将协作令牌的每次访问写入 collab_access_log</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollabSecurityService {

    /** 协作令牌专属的 Spring Security 角色 */
    public static final String COLLAB_ROLE = "ROLE_COLLAB";
    /** JWT 中区分协作令牌的 loginType 取值 */
    public static final String COLLAB_LOGIN_TYPE = "collab";

    private final CollabEntryMapper collabEntryMapper;
    private final CollabAccessLogMapper collabAccessLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 加载有效的协作入口（启用中且未过期）；不存在或已失效返回 null
     */
    public CollabEntry loadValidEntry(Long entryId) {
        if (entryId == null) {
            return null;
        }
        CollabEntry entry = collabEntryMapper.selectById(entryId);
        if (entry == null) {
            return null;
        }
        if (entry.getStatus() == null || entry.getStatus() != 1) {
            return null;
        }
        if (entry.getExpireTime() != null && entry.getExpireTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        return entry;
    }

    /**
     * 协作令牌是否允许访问当前请求。
     * <p>
     * 策略：
     * <ol>
     *     <li>明确拒绝审核处置、删除、全量导出、用户/角色/系统/字典/日志/协作管理等敏感接口</li>
     *     <li>仅允许 GET 方式访问 point/result/template/project/section/file/health 等只读业务接口</li>
     *     <li>其余一律拒绝（白名单原则）</li>
     * </ol>
     */
    public boolean isAccessAllowed(CollabEntry entry, HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. 黑名单：与方法无关的强制拒绝路径
        if (path.startsWith("/api/v1/audit/pass")
                || path.startsWith("/api/v1/audit/reject")
                || path.startsWith("/api/v1/audit/batch-pass")
                || path.startsWith("/api/v1/export")
                || path.startsWith("/api/v1/user")
                || path.startsWith("/api/v1/role")
                || path.startsWith("/api/v1/log/operation")
                || path.startsWith("/api/v1/log/login")
                || path.startsWith("/api/v1/dict")
                || path.startsWith("/api/v1/dictionary")
                || path.startsWith("/api/v1/dictionary-data")
                || path.startsWith("/api/v1/collab")
                || path.startsWith("/api/v1/system")
                || path.startsWith("/actuator")) {
            return false;
        }

        // 2. 一律禁止 DELETE
        if ("DELETE".equalsIgnoreCase(method)) {
            return false;
        }

        // 3. 白名单：协作令牌只读访问受限业务接口
        if ("GET".equalsIgnoreCase(method)) {
            boolean allowedPath = path.startsWith("/api/v1/point")
                    || path.startsWith("/api/v1/result")
                    || path.startsWith("/api/v1/template")
                    || path.startsWith("/api/v1/project")
                    || path.startsWith("/api/v1/section")
                    || path.startsWith("/api/v1/file")
                    || path.startsWith("/api/v1/health");
            return allowedPath && isObjectScopeAllowed(entry, request);
        }

        return false;
    }

    public List<Long> getAuthorizedProjectIds(CollabEntry entry) {
        return parseLongList(entry == null ? null : entry.getProjectIds());
    }

    public List<Long> getAuthorizedPointIds(CollabEntry entry) {
        return parseLongList(entry == null ? null : entry.getPointIds());
    }

    public List<String> getGrantedPermissionCodes(CollabEntry entry) {
        Set<String> configured = new HashSet<>(parseStringList(entry == null ? null : entry.getPermissions()));
        Set<String> validCodes = new HashSet<>(Arrays.asList(Permissions.getAll()));
        configured.removeIf(code -> !validCodes.contains(code));

        // 协作入口历史上是只读白名单；未配置 permissions 时给只读默认值，避免老数据全部不可用。
        if (configured.isEmpty()) {
            configured.add(Permissions.PROJECT_VIEW);
            configured.add(Permissions.POINT_VIEW);
            configured.add(Permissions.TEMPLATE_VIEW);
            configured.add(Permissions.AUDIT_VIEW);
        }
        return new ArrayList<>(configured);
    }

    public CollabEntry loadCurrentEntry(String principalName) {
        Long entryId = parseCollabPrincipal(principalName);
        return loadValidEntry(entryId);
    }

    private boolean isObjectScopeAllowed(CollabEntry entry, HttpServletRequest request) {
        if (entry == null) {
            return false;
        }
        String path = request.getRequestURI();
        Long projectId = getLongParameter(request, "projectId");
        Long pointId = getLongParameter(request, "pointId");
        List<Long> pathIds = extractPathIds(path);

        if (path.startsWith("/api/v1/project/") && !path.startsWith("/api/v1/project/page") && !pathIds.isEmpty()) {
            projectId = pathIds.get(0);
        } else if (path.startsWith("/api/v1/point/") && !pathIds.isEmpty()) {
            pointId = pathIds.get(0);
        } else if (path.startsWith("/api/v1/result/point/") && !pathIds.isEmpty()) {
            pointId = pathIds.get(0);
        }

        if (projectId != null && !getAuthorizedProjectIds(entry).contains(projectId)) {
            return false;
        }
        if (pointId != null && !getAuthorizedPointIds(entry).contains(pointId)) {
            return false;
        }
        return true;
    }

    private Long getLongParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Long> extractPathIds(String path) {
        if (!StringUtils.hasText(path)) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        for (String segment : path.split("/")) {
            if (!segment.matches("\\d+")) {
                continue;
            }
            try {
                ids.add(Long.valueOf(segment));
            } catch (NumberFormatException ignored) {
                // ignore malformed numeric segment
            }
        }
        return ids;
    }

    private Long parseCollabPrincipal(String principalName) {
        if (!StringUtils.hasText(principalName) || !principalName.startsWith("collab:")) {
            return null;
        }
        try {
            return Long.valueOf(principalName.substring("collab:".length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Long> parseLongList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<Long>>() {});
        } catch (Exception ignored) {
            List<Long> values = new ArrayList<>();
            for (String part : raw.split(",")) {
                if (!StringUtils.hasText(part)) {
                    continue;
                }
                try {
                    values.add(Long.valueOf(part.trim()));
                } catch (NumberFormatException ignoredPart) {
                    // ignore invalid id
                }
            }
            return values;
        }
    }

    private List<String> parseStringList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
        } catch (Exception ignored) {
            List<String> values = new ArrayList<>();
            for (String part : raw.split(",")) {
                String value = part.trim().replace("\"", "");
                if (StringUtils.hasText(value)) {
                    values.add(value);
                }
            }
            return values;
        }
    }

    /**
     * 写入一次协作访问日志（异常吞掉，不影响主流程）
     */
    public void logAccess(Long entryId, String token, HttpServletRequest request, int responseCode) {
        try {
            CollabAccessLog accessLog = new CollabAccessLog();
            accessLog.setEntryId(entryId);
            accessLog.setToken(token);
            accessLog.setIp(IpUtils.getClientIp(request));
            accessLog.setUserAgent(IpUtils.getUserAgent(request));
            accessLog.setRequestPath(request.getRequestURI());
            accessLog.setResponseCode(responseCode);
            accessLog.setCreateTime(LocalDateTime.now());
            collabAccessLogMapper.insert(accessLog);
        } catch (Exception e) {
            log.error("写入协作访问日志失败: {}", e.getMessage());
        }
    }
}
