package com.qhiot.survey.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

/**
 * Embedded Redis配置（仅用于测试环境）
 */
@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void postConstruct() {
        // 启动嵌入式Redis，使用随机端口
        redisServer = new RedisServer(6379);
        try {
            redisServer.start();
            System.out.println("✅ Embedded Redis started on port 6379");
        } catch (Exception e) {
            System.err.println("⚠️  Embedded Redis failed to start: " + e.getMessage());
            System.err.println("💡 This is expected if Redis is already running on port 6379");
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (redisServer != null) {
            redisServer.stop();
            System.out.println("Embedded Redis stopped");
        }
    }
}
