/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.monitor.MonitorMetricStore;
import org.eclipse.xpanse.modules.orchestrator.monitor.MetricsExporter;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.aggregates.AggregationRequest;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.filter.MetricsFilter;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.metrics.CeilometerMetricType;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.resources.InstanceNetworkResource;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.resources.InstanceResource;
import org.eclipse.xpanse.plugins.openstack.monitor.keystone.KeystoneManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

    private final CredentialCenter credentialCenter;

    private final MonitorMetricStore monitorMetricStore;

    /**
     * Constructor for the MetricsManager bean.
     *
     * @param keystoneManager               KeystoneManager bean.
     * @param resourcesService              ResourcesService bean.
     * @param gnocchiToXpanseModelConverter GnocchiToXpanseModelConverter bean.
     * @param aggregationService            AggregationService bean.
     * @param measuresService               MeasuresService bean.
     * @param monitorMetricStore            MonitorMetricStore bean.
     */
    @Autowired
    public MetricsManager(KeystoneManager keystoneManager, ResourcesService resourcesService,
                          GnocchiToXpanseModelConverter gnocchiToXpanseModelConverter,
                          AggregationService aggregationService, MeasuresService measuresService,
                          CredentialCenter credentialCenter,
                          MonitorMetricStore monitorMetricStore) {
        this.keystoneManager = keystoneManager;
        this.resourcesService = resourcesService;
        this.gnocchiToXpanseModelConverter = gnocchiToXpanseModelConverter;
        this.aggregationService = aggregationService;
        this.measuresService = measuresService;
        this.credentialCenter = credentialCenter;
        this.monitorMetricStore = monitorMetricStore;
    }

    /**
     * Method which does the actual implementation for MetricsExporter. {@link
     * MetricsExporter#getMetricsForResource} (ResourceMetricRequest metricQueryRequest)}().
     *
     * @return returns list of Metrics.
     */
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {

        keystoneManager.authenticate(credentialCenter.getCredential(
                Csp.OPENSTACK, resourceMetricRequest.getXpanseUserName(),
                CredentialType.VARIABLES));
        MonitorResourceType monitorResourceType = resourceMetricRequest.getMonitorResourceType();
        InstanceResource instanceResource =
                this.resourcesService.getInstanceResourceInfoById(
                        resourceMetricRequest.getDeployResource().getResourceId());
        List<Metric> metrics = new ArrayList<>();
        if (Objects.nonNull(instanceResource)) {
            for (Map.Entry<String, String> entry : instanceResource.getMetrics().entrySet()) {
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
            if (monitorResourceType == MonitorResourceType.VM_NETWORK_INCOMING
                    || monitorResourceType == MonitorResourceType.VM_NETWORK_OUTGOING
                    || Objects.isNull(monitorResourceType)) {
                InstanceNetworkResource instanceNetworkResource =
                        this.resourcesService.getInstanceNetworkResourceInfoByInstanceId(
                                resourceMetricRequest.getDeployResource().getResourceId());
                for (Map.Entry<String, String> entry : instanceNetworkResource.getMetrics()
                        .entrySet()) {
                    if (monitorResourceType == MonitorResourceType.VM_NETWORK_INCOMING
                            || Objects.isNull(monitorResourceType)) {
                        if (entry.getKey()
                                .equals(CeilometerMetricType.NETWORK_INCOMING.toValue())) {
                            metrics.add(getNetworkUsage(resourceMetricRequest, entry.getValue(),
                                    MonitorResourceType.VM_NETWORK_INCOMING));
                        }
                    }
                    if (monitorResourceType == MonitorResourceType.VM_NETWORK_OUTGOING
                            || Objects.isNull(monitorResourceType)) {
                        if (entry.getKey()
                                .equals(CeilometerMetricType.NETWORK_OUTGOING.toValue())) {
                            metrics.add(getNetworkUsage(resourceMetricRequest, entry.getValue(),
                                    MonitorResourceType.VM_NETWORK_OUTGOING));
                        }
                    }
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
        Metric metric = this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                resourceMetricRequest.getDeployResource(),
                MonitorResourceType.CPU,
                this.aggregationService.getAggregatedMeasuresByOperation(
                        aggregationRequest, metricsFilter).getMeasures().getAggregated(),
                MetricUnit.PERCENTAGE,
                resourceMetricRequest.isOnlyLastKnownMetric());
        doCacheActionForResourceMetrics(resourceMetricRequest, MonitorResourceType.CPU, metric);
        return metric;
    }

    private Metric getMemoryUsage(ResourceMetricRequest resourceMetricRequest, String metricId) {
        MetricsFilter metricsFilter =
                this.gnocchiToXpanseModelConverter.buildMetricsFilter(resourceMetricRequest);
        Metric metric = this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                resourceMetricRequest.getDeployResource(),
                MonitorResourceType.MEM,
                this.measuresService.getMeasurementsForResourceByMetricId(metricId, metricsFilter),
                MetricUnit.MB,
                resourceMetricRequest.isOnlyLastKnownMetric());
        doCacheActionForResourceMetrics(resourceMetricRequest, MonitorResourceType.MEM, metric);
        return metric;
    }

    private Metric getNetworkUsage(ResourceMetricRequest resourceMetricRequest, String metricId,
                                   MonitorResourceType monitorResourceType) {
        if (monitorResourceType != MonitorResourceType.VM_NETWORK_INCOMING
                && monitorResourceType != MonitorResourceType.VM_NETWORK_OUTGOING) {
            throw new IllegalArgumentException(MonitorResourceType.CPU.toValue()
                    + "is not a valid resource type for this method");
        }
        AggregationRequest aggregationRequest = this.gnocchiToXpanseModelConverter
                .buildAggregationRequestToGetNetworkRate(
                        metricId);
        MetricsFilter metricsFilter =
                this.gnocchiToXpanseModelConverter.buildMetricsFilter(resourceMetricRequest);
        Metric metric = this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                resourceMetricRequest.getDeployResource(),
                monitorResourceType,
                this.aggregationService.getAggregatedMeasuresByOperation(
                        aggregationRequest, metricsFilter).getMeasures().getAggregated(),
                MetricUnit.BYTES_PER_SECOND,
                resourceMetricRequest.isOnlyLastKnownMetric());
        doCacheActionForResourceMetrics(resourceMetricRequest, monitorResourceType, metric);
        return metric;
    }


    private void doCacheActionForResourceMetrics(ResourceMetricRequest resourceMetricRequest,
                                                 MonitorResourceType monitorResourceType,
                                                 Metric metric) {
        if (resourceMetricRequest.isOnlyLastKnownMetric()) {
            String resourceId = resourceMetricRequest.getDeployResource().getResourceId();
            if (Objects.nonNull(metric) && !CollectionUtils.isEmpty(metric.getMetrics())) {
                monitorMetricStore.storeMonitorMetric(Csp.OPENSTACK,
                        resourceId, monitorResourceType, metric);

            } else {
                Metric cacheMetric = monitorMetricStore.getMonitorMetric(Csp.OPENSTACK, resourceId,
                        monitorResourceType);
                if (Objects.nonNull(cacheMetric)
                        && !CollectionUtils.isEmpty(cacheMetric.getMetrics())) {
                    metric = cacheMetric;
                }
            }
        }
    }


}
