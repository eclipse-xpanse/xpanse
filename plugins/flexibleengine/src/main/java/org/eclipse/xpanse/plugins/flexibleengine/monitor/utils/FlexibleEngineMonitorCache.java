/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.plugin.monitor.Metric;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineMetric;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * FlexibleEngine Monitor Cache.
 */
@Slf4j
@Component
public class FlexibleEngineMonitorCache {

    private static final int DEFAULT_TIME_DIFFERENCE = 30 * 60 * 1000;
    private static final Map<String, List<FlexibleEngineMetric>> METRIC_MAP =
            new ConcurrentHashMap<>();

    /**
     * Set Metric Cache.
     */
    public void set(String resourceId, Metric metric) {
        if (StringUtils.isNotBlank(resourceId) && Objects.nonNull(metric)
                && !CollectionUtils.isEmpty(metric.getMetrics())) {
            if (METRIC_MAP.containsKey(resourceId)) {
                METRIC_MAP.get(resourceId).add(new FlexibleEngineMetric(metric));
            } else {
                List<FlexibleEngineMetric> metrics = new ArrayList<>();
                metrics.add(new FlexibleEngineMetric(metric));
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
        if (StringUtils.isBlank(resourceId) || isEmpty() || !METRIC_MAP.containsKey(resourceId)) {
            return metrics;
        }

        List<FlexibleEngineMetric> flexibleEngineMetrics = METRIC_MAP.get(resourceId);
        List<FlexibleEngineMetric> expiredCache = new ArrayList<>();
        for (FlexibleEngineMetric metricCache : flexibleEngineMetrics) {
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

}
