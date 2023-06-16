/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin.monitor;

import java.util.List;
import org.eclipse.xpanse.modules.models.monitor.Metric;

/**
 * The interface for the monitor metrics exporter.
 */
public interface MetricsExporter {

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     */
    List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest);

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric result.
     */
    List<Metric> getMetricsForResource(ResourceMetricRequest resourceMetricRequest);


    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    List<Metric> getMetricsForService(ServiceMetricRequest serviceMetricRequest);

}
