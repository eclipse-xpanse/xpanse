/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.monitor;

import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.monitor.cache.MonitorMetricCacheKey;
import org.eclipse.xpanse.modules.monitor.cache.MonitorMetricCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component which acts as the gateway to monitor metric stored in cache.
 */
@Component
public class MonitorMetricStore {

    private final MonitorMetricCacheManager monitorMetricCacheManager;

    /**
     * Constructor for ResourceMetricsStore.
     *
     * @param monitorMetricCacheManager instance of MonitorMetricCacheManager class.
     */
    @Autowired
    public MonitorMetricStore(MonitorMetricCacheManager monitorMetricCacheManager) {
        this.monitorMetricCacheManager = monitorMetricCacheManager;
    }

    /**
     * Methods to add monitor metric into store.
     *
     * @param csp        CSP to which the resource instance belongs to.
     * @param resourceId id of the resource instance.
     * @param type       tye of the resource monitor.
     * @param metric     metric object.
     */
    public void storeMonitorMetric(Csp csp, String resourceId, MonitorResourceType type,
                                   Metric metric) {
        MonitorMetricCacheKey monitorMetricCacheKey =
                new MonitorMetricCacheKey(csp, resourceId, type);
        this.monitorMetricCacheManager.put(monitorMetricCacheKey, metric);
    }


    /**
     * Methods to get monitor metric from store.
     *
     * @param csp        CSP to which the resource instance belongs to.
     * @param resourceId id of the resource instance.
     * @param type       tye of the resource monitor.
     * @return metric item list.
     */
    public Metric getMonitorMetric(Csp csp, String resourceId, MonitorResourceType type) {
        MonitorMetricCacheKey monitorMetricCacheKey =
                new MonitorMetricCacheKey(csp, resourceId, type);
        return this.monitorMetricCacheManager.get(monitorMetricCacheKey);
    }

    /**
     * Methods to remove monitor metric from store.
     *
     * @param csp        CSP to which the resource instance belongs to.
     * @param resourceId id of the resource instance.
     * @param type       tye of the resource monitor.
     */
    public void deleteMonitorMetric(Csp csp, String resourceId, MonitorResourceType type) {
        MonitorMetricCacheKey monitorMetricCacheKey =
                new MonitorMetricCacheKey(csp, resourceId, type);
        this.monitorMetricCacheManager.remove(monitorMetricCacheKey);
    }
}
