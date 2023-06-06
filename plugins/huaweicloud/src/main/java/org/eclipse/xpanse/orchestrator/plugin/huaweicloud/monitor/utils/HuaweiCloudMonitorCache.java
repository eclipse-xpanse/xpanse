/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudMetric;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Huawei Monitor Cache.
 */
@Slf4j
@Component
public class HuaweiCloudMonitorCache {

    public static final int DEFAULT_CACHE_CLEAR_TIME = 12 * 60 * 60 * 1000;
    private static final int DEFAULT_TIME_DIFFERENCE = 30 * 60 * 1000;
    private static final Map<String, List<HuaweiCloudMetric>> METRIC_MAP =
            new ConcurrentHashMap<>();
    @Setter
    @Getter
    private long lastClearTime = 0L;

    /**
     * Set Metric Cache.
     */
    public void set(String resourceId, Metric metric) {
        if (StringUtils.isNotBlank(resourceId) && Objects.nonNull(metric)
                && !CollectionUtils.isEmpty(metric.getMetrics())) {
            if (METRIC_MAP.containsKey(resourceId)) {
                METRIC_MAP.get(resourceId).add(new HuaweiCloudMetric(metric));
            } else {
                List<HuaweiCloudMetric> metrics = new ArrayList<>();
                metrics.add(new HuaweiCloudMetric(metric));
                METRIC_MAP.put(resourceId, metrics);
            }
        }
    }

    /**
     * Check whether the cache map is empty.
     */
    public boolean isEmpty() {
        return METRIC_MAP.isEmpty();
    }

    /**
     * Get the Metrics of the resource from the Cache.
     */
    public List<Metric> get(String resourceId, String metricName) {
        List<Metric> metrics = new ArrayList<>();
        if (StringUtils.isBlank(resourceId) || isEmpty()) {
            return metrics;
        }

        List<HuaweiCloudMetric> huaweiCloudMetrics = METRIC_MAP.get(resourceId);
        List<HuaweiCloudMetric> expiredCache = new ArrayList<>();
        for (HuaweiCloudMetric metricCache : huaweiCloudMetrics) {
            if (System.currentTimeMillis() - metricCache.getTime() > DEFAULT_TIME_DIFFERENCE) {
                expiredCache.add(metricCache);
            } else {
                if (StringUtils.isNotEmpty(metricName)) {
                    if (metricName.equals(metricCache.getMetric().getName())) {
                        metrics.add(metricCache.getMetric());
                    }
                }
            }
        }
        METRIC_MAP.get(resourceId).removeAll(expiredCache);
        return metrics;
    }

    /**
     * Drop the expired metrics from the Cache Map (Default 30 minutes).
     */
    @Async("taskExecutor")
    public void expire(String resourceId) {
        MDC.put("RESOURCE_ID", resourceId);
        if (METRIC_MAP.isEmpty()) {
            return;
        }
        List<HuaweiCloudMetric> huaweiCloudMetrics = METRIC_MAP.get(resourceId);
        for (HuaweiCloudMetric huaweiCloudMetric : huaweiCloudMetrics) {
            if (System.currentTimeMillis() - huaweiCloudMetric.getTime()
                    > DEFAULT_TIME_DIFFERENCE) {
                METRIC_MAP.remove(resourceId);
                log.info("The cache resource with resourceId {} is cleared ", resourceId);
            }
        }
    }
}