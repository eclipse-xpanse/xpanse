/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.monitor;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.MONITOR_METRICS_CACHE_NAME;

import jakarta.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.cache.exceptions.CacheNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/** Component which acts as the gateway to monitor metric stored in cache. */
@Slf4j
@Component
public class MonitorMetricsStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Boolean redisCacheEnabled;
    private final long monitorMetricsCacheDuration;

    /**
     * Constructor for MonitorMetricsStore.
     *
     * @param redisCacheEnabled Enable redis cache.
     * @param monitorMetricsCacheDuration monitorMetricsCacheDuration.
     * @param redisTemplate redisTemplate.
     */
    @Autowired
    public MonitorMetricsStore(
            @Value("${enable.redis.distributed.cache:false}") Boolean redisCacheEnabled,
            @Value("${service.monitor.metrics.cache.expire.time.in.minutes:60}")
                    long monitorMetricsCacheDuration,
            @Nullable RedisTemplate<String, Object> redisTemplate) {
        this.redisCacheEnabled = redisCacheEnabled;
        this.monitorMetricsCacheDuration = monitorMetricsCacheDuration;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Methods to add monitor metric into store.
     *
     * @param metric metric object.
     */
    @CachePut(cacheNames = MONITOR_METRICS_CACHE_NAME, key = "#key")
    public Metric storeMonitorMetric(MonitorMetricsCacheKey key, Metric metric) {
        log.info("Store monitor metric cache entry with key:{}", key);
        return metric;
    }

    /**
     * Methods to get monitor metric from store.
     *
     * @return metric item list.
     */
    @Cacheable(cacheNames = MONITOR_METRICS_CACHE_NAME, key = "#key")
    public Metric getMonitorMetric(MonitorMetricsCacheKey key) {
        throw new CacheNotFoundException("No monitor metric cache entry found with key: " + key);
    }

    /**
     * Methods to remove monitor metric from store.
     *
     * @param key cache key.
     */
    @CacheEvict(cacheNames = MONITOR_METRICS_CACHE_NAME, key = "#key")
    public void deleteMonitorMetric(MonitorMetricsCacheKey key) {
        log.info("Delete monitor metric cache entry with key:{}", key);
    }

    /**
     * Method to update the time-to-live of the metrics in the redis cache.
     *
     * @param cacheKey CredentialCacheKey.
     */
    public void updateMetricsCacheTimeToLive(MonitorMetricsCacheKey cacheKey) {
        if (!redisCacheEnabled || Objects.isNull(redisTemplate) || Objects.isNull(cacheKey)) {
            return;
        }
        String redisKey = MONITOR_METRICS_CACHE_NAME + "::" + cacheKey;
        long timeToLive =
                monitorMetricsCacheDuration > 0
                        ? monitorMetricsCacheDuration
                        : DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;
        try {
            Boolean flag = redisTemplate.expire(redisKey, timeToLive, TimeUnit.MINUTES);
            if (Boolean.TRUE.equals(flag)) {
                log.info(
                        "Updated expiration of the redis key:{} with the time:{} in minutes "
                                + "successfully.",
                        redisKey,
                        timeToLive);
            }
        } catch (Exception e) {
            log.error(
                    "Updated expiration of the redis key:{} with the time:{} in minutes failed.",
                    redisKey,
                    timeToLive);
        }
    }
}
