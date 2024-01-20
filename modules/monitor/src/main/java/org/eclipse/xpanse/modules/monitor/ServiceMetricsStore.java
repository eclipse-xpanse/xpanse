/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.monitor;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.monitor.cache.ServiceMetricsCacheKey;
import org.eclipse.xpanse.modules.monitor.cache.ServiceMetricsCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component which acts as the gateway to monitor metric stored in cache.
 */
@Component
public class ServiceMetricsStore {

    private final ServiceMetricsCacheManager serviceMetricsCacheManager;

    /**
     * Constructor for ResourceMetricsStore.
     *
     * @param serviceMetricsCacheManager instance of MonitorMetricCacheManager class.
     */
    @Autowired
    public ServiceMetricsStore(ServiceMetricsCacheManager serviceMetricsCacheManager) {
        this.serviceMetricsCacheManager = serviceMetricsCacheManager;
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
        ServiceMetricsCacheKey serviceMetricsCacheKey =
                new ServiceMetricsCacheKey(csp, resourceId, type);
        this.serviceMetricsCacheManager.put(serviceMetricsCacheKey, metric);
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
        ServiceMetricsCacheKey serviceMetricsCacheKey =
                new ServiceMetricsCacheKey(csp, resourceId, type);
        return this.serviceMetricsCacheManager.get(serviceMetricsCacheKey);
    }

    /**
     * Methods to remove monitor metric from store.
     *
     * @param csp        CSP to which the resource instance belongs to.
     * @param resourceId id of the resource instance.
     * @param type       tye of the resource monitor.
     */
    public void deleteMonitorMetric(Csp csp, String resourceId, MonitorResourceType type) {
        ServiceMetricsCacheKey serviceMetricsCacheKey =
                new ServiceMetricsCacheKey(csp, resourceId, type);
        this.serviceMetricsCacheManager.remove(serviceMetricsCacheKey);
    }
}
