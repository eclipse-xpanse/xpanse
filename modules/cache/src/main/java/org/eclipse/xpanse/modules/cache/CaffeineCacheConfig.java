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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.cache.credential.CredentialCacheKey;
import org.eclipse.xpanse.modules.cache.credential.CredentialCaffeineCacheExpiry;
import org.eclipse.xpanse.modules.cache.monitor.MonitorMetricsCacheKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caffeine cache configuration class.
 */
@Slf4j
@Configuration
public class CaffeineCacheConfig {

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
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(REGION_AZS_CACHE_NAME, getRegionAzsCache());
        cacheManager.registerCustomCache(SERVICE_FLAVOR_PRICE_CACHE_NAME,
                getServiceFlavorPriceCache());
        cacheManager.registerCustomCache(CREDENTIAL_CACHE_NAME, getCredentialsCache());
        cacheManager.registerCustomCache(MONITOR_METRICS_CACHE_NAME, getMonitorMetricsCache());
        return cacheManager;
    }

    private Cache<Object, Object> getRegionAzsCache() {
        long duration = regionAzsCacheDuration > 0 ? regionAzsCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return Caffeine.newBuilder().expireAfterWrite(duration, TimeUnit.MINUTES).build();
    }

    private Cache<Object, Object> getServiceFlavorPriceCache() {
        long duration = flavorPriceCacheDuration > 0 ? flavorPriceCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return Caffeine.newBuilder().expireAfterWrite(duration, TimeUnit.MINUTES).build();
    }


    private Cache<Object, Object> getCredentialsCache() {
        return Caffeine.newBuilder()
                .expireAfter(new CredentialCaffeineCacheExpiry())
                .removalListener((Object key, Object value, RemovalCause cause) -> {
                    if (Objects.nonNull(key) && key instanceof CredentialCacheKey) {
                        log.info("Credential cache removed, key: {}, cause: {}", key, cause);
                    }
                })
                .build();
    }

    private Cache<Object, Object> getMonitorMetricsCache() {
        long duration = monitorMetricsCacheDuration > 0 ? monitorMetricsCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return Caffeine.newBuilder()
                .expireAfterWrite(duration, TimeUnit.MINUTES)
                .removalListener((Object key, Object value, RemovalCause cause) -> {
                    if (Objects.nonNull(key) && key instanceof MonitorMetricsCacheKey) {
                        log.info("Monitor metrics cache removed, key: {}, cause: {}", key, cause);
                    }
                })
                .build();
    }

}
