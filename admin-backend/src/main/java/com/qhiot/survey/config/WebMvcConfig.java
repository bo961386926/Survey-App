package com.qhiot.survey.config;

import com.qhiot.survey.common.interceptor.IdempotentInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final IdempotentInterceptor idempotentInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 幂等性拦截器
        registry.addInterceptor(idempotentInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**",           // 认证接口不需要幂等性
                "/api/file/upload"        // 文件上传不需要幂等性
            );
    }
}
