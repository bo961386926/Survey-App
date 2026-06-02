package com.qhiot.survey.security;

import com.qhiot.survey.common.util.JwtUtil;
import com.qhiot.survey.entity.CollabAccessLog;
import com.qhiot.survey.entity.CollabEntry;
import com.qhiot.survey.mapper.CollabAccessLogMapper;
import com.qhiot.survey.mapper.CollabEntryMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 协作令牌安全测试
 * <p>
 * 直接覆盖 {@link CollabSecurityService} 的访问策略以及
 * {@link JwtAuthenticationFilter} 在收到 loginType=collab 令牌时的行为：
 * <ul>
 *     <li>允许的只读端点放行 + 写入访问日志</li>
 *     <li>受限端点（审核处置 / 删除 / 全量导出 / 用户 / 角色）返回 403 + 写入访问日志</li>
 *     <li>令牌作为内部 Token 时不会被当作协作 Token 处理</li>
 * </ul>
 * <p>
 * 该测试只依赖 Mockito + spring-test 的 Mock 对象，不启动 Spring 上下文，适合在不依赖数据库的环境运行。
 */
class CollabTokenSecurityTest {

    private static final String SECRET = "survey-system-secret-key-2024-survey-system-secret-key-2024";

    private CollabEntryMapper collabEntryMapper;
    private CollabAccessLogMapper collabAccessLogMapper;
    private CollabSecurityService collabSecurityService;
    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        collabEntryMapper = mock(CollabEntryMapper.class);
        collabAccessLogMapper = mock(CollabAccessLogMapper.class);
        collabSecurityService = new CollabSecurityService(collabEntryMapper, collabAccessLogMapper);

        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 7200000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L);

        userDetailsService = mock(UserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, collabSecurityService);

        SecurityContextHolder.clearContext();
    }

    private CollabEntry validEntry(Long id) {
        CollabEntry entry = new CollabEntry();
        entry.setId(id);
        entry.setEntryName("第三方-测试");
        entry.setStatus(1);
        entry.setExpireTime(LocalDateTime.now().plusDays(7));
        return entry;
    }

    @Nested
    @DisplayName("CollabSecurityService.isAccessAllowed 策略")
    class AccessPolicyTests {

        private MockHttpServletRequest req(String method, String uri) {
            MockHttpServletRequest r = new MockHttpServletRequest();
            r.setMethod(method);
            r.setRequestURI(uri);
            return r;
        }

        @Test
        @DisplayName("受限端点：审核通过 / 驳回 / 删除 / 全量导出 / 用户 / 角色 / 系统 全部拒绝")
        void restrictedEndpointsAreDenied() {
            assertThat(collabSecurityService.isAccessAllowed(req("POST", "/api/v1/audit/pass"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("POST", "/api/v1/audit/reject"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("POST", "/api/v1/audit/batch-pass"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("DELETE", "/api/v1/point/1"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("DELETE", "/api/v1/result/1"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/export/list"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/user"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("POST", "/api/v1/user"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/role"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/system/config"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/dict"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/log/operation"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/collab/page"))).isFalse();
        }

        @Test
        @DisplayName("白名单端点：GET 点位 / 结果 / 模板 / 项目 / 标段 允许")
        void whitelistedReadEndpointsAreAllowed() {
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/point/list"))).isTrue();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/result/123"))).isTrue();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/template/page"))).isTrue();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/project/1"))).isTrue();
            assertThat(collabSecurityService.isAccessAllowed(req("GET", "/api/v1/section/list"))).isTrue();
        }

        @Test
        @DisplayName("白名单端点上的写方法（POST/PUT）依然拒绝")
        void writeMethodsOnWhitelistedPathsAreDenied() {
            assertThat(collabSecurityService.isAccessAllowed(req("POST", "/api/v1/point"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("PUT", "/api/v1/result/1"))).isFalse();
            assertThat(collabSecurityService.isAccessAllowed(req("PATCH", "/api/v1/template/1"))).isFalse();
        }
    }

    @Nested
    @DisplayName("JwtAuthenticationFilter 协作令牌处理")
    class FilterBehaviorTests {

        @Test
        @DisplayName("协作令牌访问受限端点 -> 403 + 写入 collab_access_log")
        void collabTokenOnRestrictedEndpointReturns403AndLogs() throws Exception {
            Long entryId = 9001L;
            when(collabEntryMapper.selectById(entryId)).thenReturn(validEntry(entryId));

            String token = jwtUtil.generateCollabToken(entryId, "Restricted-Test", 60_000L);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("POST");
            request.setRequestURI("/api/v1/audit/pass");
            request.addHeader("Authorization", "Bearer " + token);
            request.setRemoteAddr("10.0.0.1");

            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
            verify(chain, never()).doFilter(any(), any());
            ArgumentCaptor<CollabAccessLog> captor = ArgumentCaptor.forClass(CollabAccessLog.class);
            verify(collabAccessLogMapper).insert(captor.capture());
            CollabAccessLog log = captor.getValue();
            assertThat(log.getEntryId()).isEqualTo(entryId);
            assertThat(log.getRequestPath()).isEqualTo("/api/v1/audit/pass");
            assertThat(log.getResponseCode()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
            assertThat(log.getIp()).isEqualTo("10.0.0.1");
            // 协作令牌不应触发内部 UserDetails 加载
            verifyNoInteractions(userDetailsService);
        }

        @Test
        @DisplayName("协作令牌访问白名单端点 -> 放行 + 设置 ROLE_COLLAB 鉴权 + 写入访问日志")
        void collabTokenOnAllowedEndpointPassesThroughAndLogs() throws Exception {
            Long entryId = 9002L;
            when(collabEntryMapper.selectById(entryId)).thenReturn(validEntry(entryId));

            String token = jwtUtil.generateCollabToken(entryId, "Allowed-Test", 60_000L);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/v1/point/list");
            request.addHeader("Authorization", "Bearer " + token);

            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (req, res) -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                assertThat(auth).isNotNull();
                assertThat(auth.getPrincipal()).isEqualTo("collab:" + entryId);
                assertThat(auth.getAuthorities())
                        .extracting(GrantedAuthority::getAuthority)
                        .containsExactly(CollabSecurityService.COLLAB_ROLE);
                ((HttpServletResponse) res).setStatus(200);
            };

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(200);
            verify(collabAccessLogMapper).insert(any(CollabAccessLog.class));
            verifyNoInteractions(userDetailsService);
        }

        @Test
        @DisplayName("协作令牌对应入口已撤销 -> 401 + 不放行 + 写入失败日志")
        void revokedEntryReturns401() throws Exception {
            Long entryId = 9003L;
            CollabEntry revoked = validEntry(entryId);
            revoked.setStatus(3); // 已撤销
            when(collabEntryMapper.selectById(entryId)).thenReturn(revoked);

            String token = jwtUtil.generateCollabToken(entryId, "Revoked", 60_000L);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/v1/point/list");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            verify(chain, never()).doFilter(any(), any());
            verify(collabAccessLogMapper).insert(any(CollabAccessLog.class));
        }

        @Test
        @DisplayName("内部 Token (loginType=internal) 不会被协作分支拦截")
        void internalTokenSkipsCollabBranch() throws Exception {
            String token = jwtUtil.generateAccessToken(1L, "admin", "internal");
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/v1/audit/pass");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            // 内部 Token 走标准分支，会调用 UserDetailsService；
            // 这里抛 RuntimeException 让过滤器进入 401 异常分支即可；
            // 关键是协作日志一定不能被写入。
            when(userDetailsService.loadUserByUsername(any()))
                    .thenThrow(new RuntimeException("simulated"));

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            verify(collabEntryMapper, never()).selectById(anyLong());
            verify(collabAccessLogMapper, never()).insert(any(CollabAccessLog.class));
        }
    }
}
