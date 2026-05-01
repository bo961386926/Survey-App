package com.qhiot.survey.common;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口限流拦截器
 * 基于Guava RateLimiter实现，支持按IP限流
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    /**
     * 默认每秒请求数限制
     */
    private static final double DEFAULT_PERMITS_PER_SECOND = 10.0;

    /**
     * 按IP存储限流器
     */
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIp(request);
        String uri = request.getRequestURI();

        // 对登录接口和公开接口不限流
        if (uri.startsWith("/api/auth/") || uri.startsWith("/api/public/") || 
            uri.startsWith("/doc.html") || uri.startsWith("/swagger")) {
            return true;
        }

        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(ip, 
            k -> RateLimiter.create(DEFAULT_PERMITS_PER_SECOND));

        if (!rateLimiter.tryAcquire()) {
            log.warn("接口限流: IP={}, URI={}", ip, uri);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        return true;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理代理情况下的多个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}