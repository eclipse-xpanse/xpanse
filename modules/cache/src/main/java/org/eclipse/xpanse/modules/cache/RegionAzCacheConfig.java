/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.eclipse.xpanse.modules.cache.consts.CacheNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Region AZ information cache configuration class.
 */
@Configuration
public class RegionAzCacheConfig {

    @Value("${region.azs.cache.expire.time.in.minutes:60}")
    private long duration;

    /**
     * Create the configured CaffeineCacheManager.
     *
     * @return CaffeineCacheManager
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CacheNames.REGION_AZ_CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(duration, TimeUnit.MINUTES));
        return cacheManager;
    }

}
