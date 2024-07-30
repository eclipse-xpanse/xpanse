/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.cache;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.CREDENTIAL_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.MONITOR_METRICS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.REGION_AZS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.SERVICE_FLAVOR_PRICE_CACHE_NAME;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis cache configuration class.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "enable.redis.distributed.cache", havingValue = "true")
public class RedisCacheConfig {

    @Value("${region.azs.cache.expire.time.in.minutes:60}")
    private long regionAzsCacheDuration;

    @Value("${service.flavor.price.cache.expire.time.in.minutes:60}")
    private long flavorPriceCacheDuration;

    @Value("${service.monitor.metrics.cache.expire.time.in.minutes:60}")
    private long monitorMetricsCacheDuration;

    /**
     * Config cache manager with caffeine.
     *
     * @return caffeineCacheManager
     */
    @Primary
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        checkRedisIsAvailable(connectionFactory);
        log.info("Enable cache manager with Redis.");
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(connectionFactory);
        builder.withCacheConfiguration(REGION_AZS_CACHE_NAME, getRegionAzsCache());
        builder.withCacheConfiguration(SERVICE_FLAVOR_PRICE_CACHE_NAME,
                getServiceFlavorPriceCache());
        builder.withCacheConfiguration(CREDENTIAL_CACHE_NAME, getCredentialCache());
        builder.withCacheConfiguration(MONITOR_METRICS_CACHE_NAME, getMonitorMetricsCache());
        return builder.build();
    }


    private RedisCacheConfiguration getRegionAzsCache() {
        long duration = regionAzsCacheDuration > 0 ? regionAzsCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(duration))
                .serializeKeysWith(getRedisKeySerializer())
                .serializeValuesWith(getRedisValueSerializer());
    }

    private RedisCacheConfiguration getServiceFlavorPriceCache() {
        long duration = flavorPriceCacheDuration > 0 ? flavorPriceCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(duration))
                .serializeKeysWith(getRedisKeySerializer())
                .serializeValuesWith(getRedisValueSerializer());
    }

    private RedisCacheConfiguration getCredentialCache() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES))
                .serializeKeysWith(getRedisKeySerializer())
                .serializeValuesWith(getCredentialValueSerializer());
    }

    private RedisCacheConfiguration getMonitorMetricsCache() {
        long duration = monitorMetricsCacheDuration > 0 ? monitorMetricsCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(duration))
                .serializeKeysWith(getRedisKeySerializer())
                .serializeValuesWith(getRedisValueSerializer());
    }


    /**
     * Config redis template.
     *
     * @param factory RedisConnectionFactory
     * @return redisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.afterPropertiesSet();
        return template;
    }


    /**
     * Config redis template for credential.
     *
     * @param factory RedisConnectionFactory
     * @return credentialRedisTemplate
     */
    @Bean
    public RedisTemplate<String, AbstractCredentialInfo> credentialRedisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, AbstractCredentialInfo> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(
                new Jackson2JsonRedisSerializer<>(AbstractCredentialInfo.class));
        template.afterPropertiesSet();
        return template;
    }

    private void checkRedisIsAvailable(RedisConnectionFactory connectionFactory)
            throws RuntimeException {
        try (RedisConnection ignored = connectionFactory.getConnection()) {
            log.info("Redis service is available.");
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to connect to Redis server. Error message: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error occurred while checking Redis availability. Error message: {}",
                    e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private RedisSerializationContext.SerializationPair<String> getRedisKeySerializer() {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer());
    }

    private RedisSerializationContext.SerializationPair<Object> getRedisValueSerializer() {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new Jackson2JsonRedisSerializer<>(Object.class));
    }

    private RedisSerializationContext
            .SerializationPair<AbstractCredentialInfo> getCredentialValueSerializer() {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new Jackson2JsonRedisSerializer<>(AbstractCredentialInfo.class));
    }


}
