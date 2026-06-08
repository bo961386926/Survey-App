package com.qhiot.survey.security;

import com.qhiot.survey.common.util.JwtUtil;
import com.qhiot.survey.common.util.TenantContext;
import com.qhiot.survey.entity.CollabEntry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * <p>
 * 支持两种 Token：
 * <ul>
 *     <li>内部 Token（loginType=internal 或缺省）：走标准 UserDetails 流程</li>
 *     <li>协作 Token（loginType=collab）：以 {@link CollabSecurityService#COLLAB_ROLE} 身份认证，
 *         按白名单策略限制可访问端点，并将每次请求记入 collab_access_log</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final CollabSecurityService collabSecurityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        TenantContext.clear();
        try {
            String token = getTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 解析并设置租户上下文
                Long tenantId = jwtUtil.getTenantIdFromToken(token);
                TenantContext.setTenantId(tenantId);

                String loginType = jwtUtil.getLoginType(token);

                if (CollabSecurityService.COLLAB_LOGIN_TYPE.equals(loginType)) {
                    // 协作令牌：单独的认证 + 鉴权 + 审计链路
                    handleCollabToken(token, request, response, filterChain);
                    return;
                }

                String username = jwtUtil.getUsernameFromToken(token);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 协作令牌处理：校验入口、白名单鉴权、写入访问日志后再放行。
     */
    private void handleCollabToken(String token, HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        Long entryId = jwtUtil.getCollabEntryIdFromToken(token);
        CollabEntry entry = collabSecurityService.loadValidEntry(entryId);

        if (entry == null) {
            collabSecurityService.logAccess(entryId, token, request, HttpServletResponse.SC_UNAUTHORIZED);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"协作入口已失效或不存在\",\"data\":null}");
            return;
        }

        if (!collabSecurityService.isAccessAllowed(entry, request)) {
            collabSecurityService.logAccess(entryId, token, request, HttpServletResponse.SC_FORBIDDEN);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"协作令牌无权访问该资源\",\"data\":null}");
            return;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(CollabSecurityService.COLLAB_ROLE));
        collabSecurityService.getGrantedPermissionCodes(entry).stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "collab:" + entryId,
                        null,
                        authorities
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } finally {
            collabSecurityService.logAccess(entryId, token, request, response.getStatus());
        }
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
