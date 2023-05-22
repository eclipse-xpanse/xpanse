/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.MetricItem;
import org.eclipse.xpanse.modules.monitor.enums.MetricItemType;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.aggregates.AggregationRequest;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.measures.Measure;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class to encapsulate all methods to convert between Gnocchi and Xpanse objects.
 */
@Component
public class GnocchiToXpanseModelConverter {

    /**
     * Convert Gnocchi Measures object to Xpanse Metric object.
     */
    public Metric convertGnocchiMeasuresToMetric(DeployResource deployResource,
                                                 MonitorResourceType monitorResourceType,
                                                 List<Measure> measures,
                                                 MetricUnit metricUnit) {
        Metric metric = new Metric();
        metric.setName(monitorResourceType.toValue());
        Map<String, String> labels = new HashMap<>();
        labels.put("id", deployResource.getResourceId());
        labels.put("name", deployResource.getName());
        metric.setUnit(metricUnit);
        metric.setLabels(labels);
        metric.setType(MetricType.GAUGE);
        if (!CollectionUtils.isEmpty(measures)) {
            MetricItem metricItem = new MetricItem();
            metricItem.setType(MetricItemType.VALUE);
            metricItem.setValue(measures.get(measures.size() - 1).getValue());
            metric.setMetrics(List.of(metricItem));
        }
        return metric;
    }

    /**
     * Build AggregationRequest. From the Stein release, Ceilometer has stopped generating cpu_util
     * metrics. Hence, it is necessary to convert the cpu metric which is the absolute CPU value
     * to percentage.
     *
     * @param metricId ID of the metric.
     * @return AggregationRequest object.
     */
    public AggregationRequest buildAggregationRequestToGetCpuMeasureAsPercentage(String metricId) {
        String operationString =
                String.format("(* (/ (aggregate rate:mean (metric %s mean)) 60000000000.0) 100)",
                        metricId);
        AggregationRequest aggregationRequest = new AggregationRequest();
        aggregationRequest.setOperations(operationString);
        return aggregationRequest;
    }
}

