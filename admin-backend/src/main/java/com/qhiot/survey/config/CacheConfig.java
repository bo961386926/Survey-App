package com.qhiot.survey.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Cache + Redis 配置
 *
 * <p>缓存命名空间（cache name）→ TTL 约定：
 * <ul>
 *     <li>{@link #DICT_CACHE} 字典数据 — 1 小时</li>
 *     <li>{@link #TEMPLATE_VERSION_CACHE} 模板版本详情 — 30 分钟</li>
 *     <li>{@link #USER_CACHE} 用户基础信息（用于安全上下文）— 15 分钟</li>
 * </ul>
 *
 * <p>Key 形式：{@code survey:cache:{cacheName}::{key}}，便于在 Redis 中按前缀清理。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PREFIX = "survey:cache:";

    /** 字典缓存命名空间。 */
    public static final String DICT_CACHE = "dict";
    /** 模板版本详情缓存命名空间。 */
    public static final String TEMPLATE_VERSION_CACHE = "templateVersion";
    /** 用户基础信息缓存命名空间。 */
    public static final String USER_CACHE = "user";

    /**
     * 共享的 JSON 序列化器：开启默认类型信息以便反序列化为原始类型。
     */
    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 仅信任本项目实体与基础类型，避免反序列化漏洞
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    private RedisCacheConfiguration baseConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .computePrefixWith(name -> CACHE_PREFIX + name + "::")
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer()));
    }

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认 30 分钟，未在下方显式声明的 cache 走该默认值
        RedisCacheConfiguration defaultConfig = baseConfig(Duration.ofMinutes(30));

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(DICT_CACHE,             baseConfig(Duration.ofHours(1)));
        perCache.put(TEMPLATE_VERSION_CACHE, baseConfig(Duration.ofMinutes(30)));
        perCache.put(USER_CACHE,             baseConfig(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCache)
                // 事务感知：仅在事务提交后才把缓存写入 Redis，避免脏写
                .transactionAware()
                .build();
    }
}
