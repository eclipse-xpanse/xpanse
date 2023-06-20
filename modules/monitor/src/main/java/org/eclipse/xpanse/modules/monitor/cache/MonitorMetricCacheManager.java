/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.monitor.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.springframework.stereotype.Component;

/**
 * Class which instantiates metrics cache and handles all read-write access to the cache.
 */
@Component
@Slf4j
@SuppressWarnings("UnnecessarilyFullyQualified")
public class MonitorMetricCacheManager {

    private static final Cache<MonitorMetricCacheKey, Metric> RESOURCE_METRICS_CACHE =
            monitorMetricCache();

    private static Cache<MonitorMetricCacheKey, Metric> monitorMetricCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .removalListener(
                        (MonitorMetricCacheKey key,
                         Metric graph,
                         RemovalCause cause) -> {
                            assert key != null;
                            log.info(String.format(
                                    "MetricsCache entry for csp %s resourceId %s and type %s "
                                            + "was removed. Reason - %s",
                                    key.csp(), key.resourceId(),
                                    key.monitorResourceType(), cause.toString()));
                        })
                .build();
    }

    public void put(MonitorMetricCacheKey key, Metric value) {
        RESOURCE_METRICS_CACHE.put(key, value);
    }

    public Metric get(MonitorMetricCacheKey key) {
        return RESOURCE_METRICS_CACHE.getIfPresent(key);
    }

    public void remove(MonitorMetricCacheKey key) {
        RESOURCE_METRICS_CACHE.invalidate(key);
    }

}
