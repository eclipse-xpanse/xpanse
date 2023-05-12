/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudMetric;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudMonitorMetrics;
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
    private static final Map<String, HuaweiCloudMetric> METRIC_MAP = new ConcurrentHashMap<>();
    @Setter
    @Getter
    private long lastClearTime = 0L;

    /**
     * Set Metric Cache.
     */
    public void set(String resourceId, List<Metric> metrics) {
        if (StringUtils.isBlank(resourceId) || CollectionUtils.isEmpty(metrics)) {
            return;
        }
        HuaweiCloudMetric huaweiCloudMetric = new HuaweiCloudMetric(metrics);
        METRIC_MAP.put(resourceId, huaweiCloudMetric);
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
    public List<Metric> get(String resourceId, MonitorResourceType monitorResourceType) {
        if (StringUtils.isBlank(resourceId) || isEmpty()) {
            return new ArrayList<>();
        }
        HuaweiCloudMetric huaweiCloudMetric = METRIC_MAP.get(resourceId);

        if (System.currentTimeMillis() - huaweiCloudMetric.getTime() > DEFAULT_TIME_DIFFERENCE) {
            return new ArrayList<>();
        }

        if (monitorResourceType != null) {
            if (monitorResourceType == MonitorResourceType.CPU) {
                return huaweiCloudMetric.getMetrics().stream()
                        .filter(metric -> HuaweiCloudMonitorMetrics.CPU_UTILIZED
                                == metric.getName())
                        .collect(Collectors.toList());
            } else if (monitorResourceType == MonitorResourceType.MEM) {
                return huaweiCloudMetric.getMetrics().stream()
                        .filter(metric -> HuaweiCloudMonitorMetrics.MEM_UTILIZED
                                == metric.getName())
                        .collect(Collectors.toList());
            }

        }
        return huaweiCloudMetric.getMetrics();
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
        HuaweiCloudMetric huaweiCloudMetric = METRIC_MAP.get(resourceId);
        if (System.currentTimeMillis() - huaweiCloudMetric.getTime() > DEFAULT_TIME_DIFFERENCE) {
            METRIC_MAP.remove(resourceId);
            log.info("The cache resource with resourceId {} is cleared ", resourceId);
        }
    }
}
