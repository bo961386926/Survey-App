package com.qhiot.survey.common.interceptor;

import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.annotation.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * API幂等性拦截器
 * 防止重复提交
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentInterceptor implements HandlerInterceptor {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String IDEMPOTENT_TOKEN_KEY = "idempotent:token:";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);
        
        if (idempotent == null) {
            return true;
        }
        
        // 获取幂等性Token
        String token = request.getHeader("X-Idempotent-Token");
        
        if (token == null || token.isEmpty()) {
            throw new BusinessException("缺少幂等性Token，请在请求头添加X-Idempotent-Token");
        }
        
        String redisKey = IDEMPOTENT_TOKEN_KEY + token;
        Boolean exists = redisTemplate.hasKey(redisKey);
        
        if (Boolean.FALSE.equals(exists)) {
            throw new BusinessException(idempotent.message());
        }
        
        // 删除Token（一次性使用）
        redisTemplate.delete(redisKey);
        
        log.debug("幂等性校验通过: token={}", token);
        return true;
    }
}
