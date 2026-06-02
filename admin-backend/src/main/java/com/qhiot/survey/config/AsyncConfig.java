package com.qhiot.survey.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 * 用于配置异步操作的线程池
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 操作日志记录线程池
     */
    @Bean("operationLogExecutor")
    public Executor operationLogExecutor() {
        log.info("====== [异步配置] 开始初始化操作日志线程池 ======");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(5);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名称前缀
        executor.setThreadNamePrefix("op-log-");
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("====== [异步配置] 操作日志异步线程池初始化完成 ======");
        log.info("[异步配置] 线程池配置 - corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}", 
            executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * 导出任务专用线程池
     */
    @Bean("exportTaskExecutor")
    public Executor exportTaskExecutor() {
        log.info("====== [异步配置] 开始初始化导出任务线程池 ======");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("export-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();

        log.info("====== [异步配置] 导出任务线程池初始化完成 ======");
        return executor;
    }

    /**
     * 通知发送线程池（短信、邮件等）
     * 与操作日志线程池隔离，避免互相阻塞
     */
    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        log.info("====== [异步配置] 开始初始化通知发送线程池 ======");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("notify-");
        // 队列已满时由调用线程处理，避免丢失通知
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("====== [异步配置] 通知发送线程池初始化完成 ======");
        return executor;
    }
}
