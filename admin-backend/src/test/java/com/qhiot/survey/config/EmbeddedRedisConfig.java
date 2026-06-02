package com.qhiot.survey.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Embedded Redis配置（仅用于测试环境）
 * 当 Docker Redis 不可用时，自动启动嵌入式 Redis 作为替代。
 * 若端口已被占用（如 Docker Redis 已在运行），则跳过启动。
 */
@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    /**
     * 检查端口是否已被占用
     */
    private boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false; // 端口可用
        } catch (IOException e) {
            return true;  // 端口已被占用
        }
    }

    @PostConstruct
    public void postConstruct() {
        int port = 6379;
        if (isPortInUse(port)) {
            System.out.println("✅ Port 6379 already in use — assuming external Redis is available, skipping embedded Redis");
            return;
        }
        // 启动嵌入式Redis
        redisServer = new RedisServer(port);
        try {
            redisServer.start();
            System.out.println("✅ Embedded Redis started on port " + port);
        } catch (Exception e) {
            System.err.println("⚠️  Embedded Redis failed to start: " + e.getMessage());
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
