package com.qhiot.survey.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import com.qhiot.survey.common.util.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * MyBatis-Plus配置
 * 包含：分页插件、乐观锁插件、雪花ID生成器配置、多租户拦截器
 */
@Configuration
public class MybatisPlusConfig {

    /** 多租户忽略表（系统表、日志表、空间数据表） */
    private static final List<String> IGNORE_TABLES = List.of(
            "sys_",              // 所有系统表
            "operation_log",     // 操作日志
            "login_log",         // 登录日志
            "location_correction_log" // 纠偏轨迹（空间数据）
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 多租户拦截器（必须放在分页插件之前）
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = TenantContext.getTenantId();
                // 系统管理员（tenantId == null）→ 不追加租户条件
                if (tenantId == null) return null;
                return new LongValue(tenantId);
            }

            @Override
            public boolean ignoreTable(String tableName) {
                for (String prefix : IGNORE_TABLES) {
                    if (tableName.startsWith(prefix)) return true;
                }
                return false;
            }
        }));

        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 单页最大1000条
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 配置雪花ID生成器
     * 所有使用 @TableId(type = IdType.ASSIGN_ID) 的实体都会使用这个生成器
     */
    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new SnowflakeIdGenerator();
    }
}
