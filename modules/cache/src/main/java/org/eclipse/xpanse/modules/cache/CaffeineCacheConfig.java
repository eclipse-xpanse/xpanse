/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.cache;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.REGION_AZ_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.SERVICE_FLAVOR_PRICE_CACHE_NAME;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caffeine cache configuration class.
 */
@Configuration
public class CaffeineCacheConfig {

    private static final long DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES = 60L;

    @Value("${region.azs.cache.expire.time.in.minutes:60}")
    private long regionAzsCacheDuration;

    @Value("${service.flavor.price.cache.expire.time.in.minutes:60}")
    private long flavorPriceCacheDuration;

    /**
     * Config cache manager with caffeine.
     *
     * @return caffeineCacheManager
     */
    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(REGION_AZ_CACHE_NAME, getRegionAzCache());
        cacheManager.registerCustomCache(SERVICE_FLAVOR_PRICE_CACHE_NAME,
                getServiceFlavorPriceCache());
        return cacheManager;
    }

    private Cache<Object, Object> getRegionAzCache() {
        long duration = regionAzsCacheDuration > 0 ? regionAzsCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return Caffeine.newBuilder().expireAfterWrite(duration, TimeUnit.MINUTES).build();
    }

    private Cache<Object, Object> getServiceFlavorPriceCache() {
        long duration = flavorPriceCacheDuration > 0 ? flavorPriceCacheDuration
                : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        return Caffeine.newBuilder().expireAfterWrite(duration, TimeUnit.MINUTES).build();
    }
}
