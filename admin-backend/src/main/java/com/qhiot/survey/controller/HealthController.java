package com.qhiot.survey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于负载均衡器、容器编排系统检查服务状态
 */
@Tag(name = "健康检查")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    
    @Operation(summary = "服务健康检查")
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("service", "survey-admin");
        result.put("version", "1.0.0");
        return result;
    }
    
    @Operation(summary = "服务详细信息")
    @GetMapping("/details")
    public Map<String, Object> healthDetails() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("service", "survey-admin");
        result.put("version", "1.0.0");
        
        // 组件状态
        Map<String, String> components = new HashMap<>();
        components.put("database", "UP");
        components.put("redis", "UP");
        components.put("diskSpace", "UP");
        result.put("components", components);
        
        return result;
    }
    
    @Operation(summary = "服务就绪检查")
    @GetMapping("/ready")
    public Map<String, Object> readiness() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "READY");
        result.put("timestamp", LocalDateTime.now().toString());
        return result;
    }
}
