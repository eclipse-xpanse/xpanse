/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.cache;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.CACHE_PROVIDER_REDIS;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.CREDENTIAL_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEFAULT_CREDENTIAL_CACHE_EXPIRE_TIME_IN_SECONDS;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEPLOYER_VERSIONS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.MONITOR_METRICS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.REGION_AZS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.SERVICE_FLAVOR_PRICE_CACHE_NAME;

import jakarta.annotation.Resource;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
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
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis cache configuration class.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "enable.redis.distributed.cache", havingValue = "true")
public class RedisCacheConfig {

    @Resource
    private RedisConnectionFactory connectionFactory;

    @Value("${region.azs.cache.expire.time.in.minutes:60}")
    private long regionAzsCacheDuration;

    @Value("${service.flavor.price.cache.expire.time.in.minutes:60}")
    private long flavorPriceCacheDuration;

    @Value("${service.monitor.metrics.cache.expire.time.in.minutes:60}")
    private long monitorMetricsCacheDuration;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    /**
     * Config cache manager with caffeine.
     *
     * @return caffeineCacheManager
     */
    @Primary
    @Bean
    public CacheManager redisCacheManager() {
        checkRedisIsAvailable();
        log.info("Enable cache manager with Redis.");
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(connectionFactory);
        builder.withCacheConfiguration(REGION_AZS_CACHE_NAME, getRegionAzsCache());
        builder.withCacheConfiguration(SERVICE_FLAVOR_PRICE_CACHE_NAME,
                getServiceFlavorPriceCache());
        builder.withCacheConfiguration(CREDENTIAL_CACHE_NAME, getCredentialCache());
        builder.withCacheConfiguration(MONITOR_METRICS_CACHE_NAME, getMonitorMetricsCache());
        builder.withCacheConfiguration(DEPLOYER_VERSIONS_CACHE_NAME, getDeployerVersionsCache());
        return builder.build();
    }


    /**
     * Check status redis cache.
     *
     * @return BackendSystemStatus
     */
    public BackendSystemStatus getRedisCacheStatus() {
        BackendSystemStatus backendSystemStatus = new BackendSystemStatus();
        backendSystemStatus.setBackendSystemType(BackendSystemType.CACHE_PROVIDER);
        backendSystemStatus.setName(CACHE_PROVIDER_REDIS);
        backendSystemStatus.setEndpoint(getRedisEndpoint());
        try {
            checkRedisIsAvailable();
            backendSystemStatus.setHealthStatus(HealthStatus.OK);
        } catch (RuntimeException e) {
            backendSystemStatus.setHealthStatus(HealthStatus.NOK);
            backendSystemStatus.setDetails(e.getMessage());
        }
        return backendSystemStatus;
    }

    private String getRedisEndpoint() {
        try {
            LettuceConnectionFactory lettuceConnectionFactory =
                    (LettuceConnectionFactory) connectionFactory;
            RedisStandaloneConfiguration config =
                    lettuceConnectionFactory.getStandaloneConfiguration();
            return config.getHostName() + ":" + config.getPort();
        } catch (Exception e) {
            log.error("Failed to get redis endpoint by connectionFactory.", e);
            return redisHost + ":" + redisPort;
        }
    }

    private RedisCacheConfiguration getRegionAzsCache() {
        long duration = regionAzsCacheDuration > 0 ? regionAzsCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(duration))
                .serializeKeysWith(getStringRedisSerializer())
                .serializeValuesWith(getJsonRedisSerializer());
    }

    private RedisCacheConfiguration getServiceFlavorPriceCache() {
        long duration = flavorPriceCacheDuration > 0 ? flavorPriceCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(duration))
                .serializeKeysWith(getStringRedisSerializer())
                .serializeValuesWith(getJsonRedisSerializer());
    }

    private RedisCacheConfiguration getCredentialCache() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(DEFAULT_CREDENTIAL_CACHE_EXPIRE_TIME_IN_SECONDS))
                .serializeKeysWith(getStringRedisSerializer())
                .serializeValuesWith(getCredentialValueSerializer());
    }

    private RedisCacheConfiguration getMonitorMetricsCache() {
        long duration = monitorMetricsCacheDuration > 0 ? monitorMetricsCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(duration))
                .serializeKeysWith(getStringRedisSerializer())
                .serializeValuesWith(getJsonRedisSerializer());
    }

    private RedisCacheConfiguration getDeployerVersionsCache() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(getStringRedisSerializer())
                .serializeValuesWith(getJdkRedisSerializer());
    }


    /**
     * Config redis template.
     *
     * @return redisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }


    /**
     * Config redis template for credential.
     *
     * @return credentialRedisTemplate
     */
    @Bean
    public RedisTemplate<String, AbstractCredentialInfo> credentialRedisTemplate() {
        RedisTemplate<String, AbstractCredentialInfo> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(
                new Jackson2JsonRedisSerializer<>(AbstractCredentialInfo.class));
        template.afterPropertiesSet();
        return template;
    }

    private void checkRedisIsAvailable() throws RuntimeException {
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


    private RedisSerializationContext.SerializationPair<String> getStringRedisSerializer() {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer());
    }

    // Jackson2JsonRedisSerializer for Object.
    private RedisSerializationContext.SerializationPair<Object> getJsonRedisSerializer() {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new Jackson2JsonRedisSerializer<>(Object.class));
    }

    // JdkSerializationRedisSerializer for Set, Array, Map etc.
    private RedisSerializationContext.SerializationPair<Object> getJdkRedisSerializer() {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new JdkSerializationRedisSerializer());
    }

    private RedisSerializationContext
            .SerializationPair<AbstractCredentialInfo> getCredentialValueSerializer() {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new Jackson2JsonRedisSerializer<>(AbstractCredentialInfo.class));
    }


}
