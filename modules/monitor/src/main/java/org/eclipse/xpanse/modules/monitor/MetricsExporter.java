/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.List;

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

}
