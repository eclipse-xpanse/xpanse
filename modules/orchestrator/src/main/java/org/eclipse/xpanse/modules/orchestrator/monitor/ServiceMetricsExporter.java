/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.monitor;

import java.util.List;
import org.eclipse.xpanse.modules.models.monitor.Metric;

/** The interface for the monitor metrics exporter. */
public interface ServiceMetricsExporter {

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric result.
     */
    List<Metric> getMetricsForResource(ResourceMetricsRequest resourceMetricRequest);

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest);
}
