package com.qhiot.survey.security;

import com.qhiot.survey.common.util.IpUtils;
import com.qhiot.survey.entity.CollabAccessLog;
import com.qhiot.survey.entity.CollabEntry;
import com.qhiot.survey.mapper.CollabAccessLogMapper;
import com.qhiot.survey.mapper.CollabEntryMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
    public boolean isAccessAllowed(HttpServletRequest request) {
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
            return path.startsWith("/api/v1/point")
                    || path.startsWith("/api/v1/result")
                    || path.startsWith("/api/v1/template")
                    || path.startsWith("/api/v1/project")
                    || path.startsWith("/api/v1/section")
                    || path.startsWith("/api/v1/file")
                    || path.startsWith("/api/v1/health");
        }

        return false;
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
