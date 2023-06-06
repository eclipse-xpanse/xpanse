/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.aggregates.AggregationRequest;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.filter.MetricsFilter;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.metrics.CeilometerMetricType;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.resources.Resource;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.keystone.KeystoneManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to encapsulate all Metric related public methods for Openstack plugin.
 */
@Component
public class MetricsManager {

    private final KeystoneManager keystoneManager;

    private final ResourcesService resourcesService;

    private final GnocchiToXpanseModelConverter gnocchiToXpanseModelConverter;

    private final AggregationService aggregationService;

    private final MeasuresService measuresService;

    /**
     * Constructor for the MetricsManager bean.
     *
     * @param keystoneManager               KeystoneManager bean.
     * @param resourcesService              ResourcesService bean.
     * @param gnocchiToXpanseModelConverter GnocchiToXpanseModelConverter bean.
     * @param aggregationService            AggregationService bean.
     * @param measuresService               MeasuresService bean.
     */
    @Autowired
    public MetricsManager(KeystoneManager keystoneManager, ResourcesService resourcesService,
                          GnocchiToXpanseModelConverter gnocchiToXpanseModelConverter,
                          AggregationService aggregationService, MeasuresService measuresService) {
        this.keystoneManager = keystoneManager;
        this.resourcesService = resourcesService;
        this.gnocchiToXpanseModelConverter = gnocchiToXpanseModelConverter;
        this.aggregationService = aggregationService;
        this.measuresService = measuresService;
    }

    /**
     * Method which does the actual implementation for MetricsExporter.
     * {@link org.eclipse.xpanse.modules.monitor.MetricsExporter#getMetrics(
     *ResourceMetricRequest metricQueryRequest)}().
     *
     * @return returns list of Metrics.
     */
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {

        keystoneManager.authenticate(resourceMetricRequest.getCredential());
        MonitorResourceType monitorResourceType = resourceMetricRequest.getMonitorResourceType();
        Resource resource =
                this.resourcesService.getInstanceResourceInfoById(
                        resourceMetricRequest.getDeployResource().getResourceId());
        List<Metric> metrics = new ArrayList<>();
        for (Map.Entry<String, String> entry : resource.getMetrics().entrySet()) {
            if (monitorResourceType == MonitorResourceType.CPU
                    || Objects.isNull(monitorResourceType)) {
                if (entry.getKey().equals(CeilometerMetricType.CPU.toValue())) {
                    metrics.add(getCpuUsage(resourceMetricRequest, entry.getValue()));
                }
            }
            if (monitorResourceType == MonitorResourceType.MEM
                    || Objects.isNull(monitorResourceType)) {
                if (entry.getKey().equals(CeilometerMetricType.MEMORY_USAGE.toValue())) {
                    metrics.add(getMemoryUsage(resourceMetricRequest, entry.getValue()));
                }
            }
        }
        return metrics;
    }

    private Metric getCpuUsage(ResourceMetricRequest resourceMetricRequest, String metricId) {
        AggregationRequest aggregationRequest = this.gnocchiToXpanseModelConverter
                .buildAggregationRequestToGetCpuMeasureAsPercentage(
                        metricId);
        MetricsFilter metricsFilter =
                this.gnocchiToXpanseModelConverter.buildMetricsFilter(resourceMetricRequest);
        return this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                resourceMetricRequest.getDeployResource(),
                MonitorResourceType.CPU,
                this.aggregationService.getAggregatedMeasuresByOperation(
                        aggregationRequest, metricsFilter).getMeasures().getAggregated(),
                MetricUnit.PERCENTAGE);
    }

    private Metric getMemoryUsage(ResourceMetricRequest resourceMetricRequest, String metricId) {
        MetricsFilter metricsFilter =
                this.gnocchiToXpanseModelConverter.buildMetricsFilter(resourceMetricRequest);
        return this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                resourceMetricRequest.getDeployResource(),
                MonitorResourceType.MEM,
                this.measuresService.getMeasurementsForResourceByMetricId(metricId, metricsFilter),
                MetricUnit.MB);
    }

}
