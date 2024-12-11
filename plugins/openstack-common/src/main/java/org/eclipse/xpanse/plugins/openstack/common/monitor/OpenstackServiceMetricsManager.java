/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.monitor;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.cache.monitor.MonitorMetricsCacheKey;
import org.eclipse.xpanse.modules.cache.monitor.MonitorMetricsStore;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsExporter;
import org.eclipse.xpanse.plugins.openstack.common.auth.ProviderAuthInfoResolver;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.aggregates.AggregationRequest;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.filter.MetricsFilter;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.metrics.CeilometerMetricType;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.resources.InstanceNetworkResource;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.resources.InstanceResource;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.utils.GnocchiToXpanseModelConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Class to encapsulate all Metric related public methods for Openstack plugin. */
@Slf4j
@Component
public class OpenstackServiceMetricsManager {

    @Resource private ResourcesService resourcesService;
    @Resource private GnocchiToXpanseModelConverter gnocchiToXpanseModelConverter;
    @Resource private AggregationService aggregationService;
    @Resource private MeasuresService measuresService;
    @Resource private MonitorMetricsStore monitorMetricsStore;
    @Resource private ProviderAuthInfoResolver providerAuthInfoResolver;

    /**
     * Method which does the actual implementation for MetricsExporter. {@link
     * ServiceMetricsExporter#getMetricsForResource} (ResourceMetricRequest metricQueryRequest)().
     *
     * @return returns list of Metrics.
     */
    public List<Metric> getMetrics(Csp csp, ResourceMetricsRequest request) {
        List<Metric> metrics = new ArrayList<>();
        String userId = request.getUserId();
        UUID serviceId = request.getServiceId();
        String siteName = request.getRegion().getSite();
        String resourceId = request.getDeployResource().getResourceId();
        try {
            MonitorResourceType monitorResourceType = request.getMonitorResourceType();
            providerAuthInfoResolver.getAuthenticatedClientForCsp(
                    csp, siteName, userId, serviceId, null);
            InstanceResource instanceResource =
                    this.resourcesService.getInstanceResourceInfoById(resourceId);
            if (Objects.nonNull(instanceResource)) {
                for (Map.Entry<String, String> entry : instanceResource.getMetrics().entrySet()) {
                    if (monitorResourceType == MonitorResourceType.CPU
                            || Objects.isNull(monitorResourceType)) {
                        if (entry.getKey().equals(CeilometerMetricType.CPU.toValue())) {
                            metrics.add(getCpuUsage(request, entry.getValue()));
                        }
                    }
                    if (monitorResourceType == MonitorResourceType.MEM
                            || Objects.isNull(monitorResourceType)) {
                        if (entry.getKey().equals(CeilometerMetricType.MEMORY_USAGE.toValue())) {
                            metrics.add(getMemoryUsage(request, entry.getValue()));
                        }
                    }
                }
                if (monitorResourceType == MonitorResourceType.VM_NETWORK_INCOMING
                        || monitorResourceType == MonitorResourceType.VM_NETWORK_OUTGOING
                        || Objects.isNull(monitorResourceType)) {
                    InstanceNetworkResource instanceNetworkResource =
                            this.resourcesService.getInstanceNetworkResourceInfoByInstanceId(
                                    request.getDeployResource().getResourceId());
                    for (Map.Entry<String, String> entry :
                            instanceNetworkResource.getMetrics().entrySet()) {
                        if (monitorResourceType == MonitorResourceType.VM_NETWORK_INCOMING
                                || Objects.isNull(monitorResourceType)) {
                            if (entry.getKey()
                                    .equals(CeilometerMetricType.NETWORK_INCOMING.toValue())) {
                                metrics.add(
                                        getNetworkUsage(
                                                request,
                                                entry.getValue(),
                                                MonitorResourceType.VM_NETWORK_INCOMING));
                            }
                        }
                        if (monitorResourceType == MonitorResourceType.VM_NETWORK_OUTGOING
                                || Objects.isNull(monitorResourceType)) {
                            if (entry.getKey()
                                    .equals(CeilometerMetricType.NETWORK_OUTGOING.toValue())) {
                                metrics.add(
                                        getNetworkUsage(
                                                request,
                                                entry.getValue(),
                                                MonitorResourceType.VM_NETWORK_OUTGOING));
                            }
                        }
                    }
                }
            }
            return metrics;
        } catch (Exception e) {
            log.error("Get metrics of resource {} failed.", resourceId);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    private Metric getCpuUsage(ResourceMetricsRequest request, String metricId) {
        AggregationRequest aggregationRequest =
                this.gnocchiToXpanseModelConverter
                        .buildAggregationRequestToGetCpuMeasureAsPercentage(metricId);
        MetricsFilter metricsFilter =
                this.gnocchiToXpanseModelConverter.buildMetricsFilter(request);
        Metric metric =
                this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                        request.getDeployResource(),
                        MonitorResourceType.CPU,
                        this.aggregationService
                                .getAggregatedMeasuresByOperation(aggregationRequest, metricsFilter)
                                .getMeasures()
                                .getAggregated(),
                        MetricUnit.PERCENTAGE,
                        request.isOnlyLastKnownMetric());
        doCacheActionForResourceMetrics(request, MonitorResourceType.CPU, metric);
        return metric;
    }

    private Metric getMemoryUsage(ResourceMetricsRequest request, String metricId) {
        MetricsFilter metricsFilter =
                this.gnocchiToXpanseModelConverter.buildMetricsFilter(request);
        Metric metric =
                this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                        request.getDeployResource(),
                        MonitorResourceType.MEM,
                        this.measuresService.getMeasurementsForResourceByMetricId(
                                metricId, metricsFilter),
                        MetricUnit.MB,
                        request.isOnlyLastKnownMetric());
        doCacheActionForResourceMetrics(request, MonitorResourceType.MEM, metric);
        return metric;
    }

    private Metric getNetworkUsage(
            ResourceMetricsRequest request,
            String metricId,
            MonitorResourceType monitorResourceType) {
        AggregationRequest aggregationRequest =
                this.gnocchiToXpanseModelConverter.buildAggregationRequestToGetNetworkRate(
                        metricId);
        MetricsFilter metricsFilter =
                this.gnocchiToXpanseModelConverter.buildMetricsFilter(request);
        Metric metric =
                this.gnocchiToXpanseModelConverter.convertGnocchiMeasuresToMetric(
                        request.getDeployResource(),
                        monitorResourceType,
                        this.aggregationService
                                .getAggregatedMeasuresByOperation(aggregationRequest, metricsFilter)
                                .getMeasures()
                                .getAggregated(),
                        MetricUnit.BYTES_PER_SECOND,
                        request.isOnlyLastKnownMetric());
        doCacheActionForResourceMetrics(request, monitorResourceType, metric);
        return metric;
    }

    private void doCacheActionForResourceMetrics(
            ResourceMetricsRequest request,
            MonitorResourceType monitorResourceType,
            Metric metric) {
        if (request.isOnlyLastKnownMetric()) {
            String resourceId = request.getDeployResource().getResourceId();
            MonitorMetricsCacheKey key =
                    new MonitorMetricsCacheKey(
                            Csp.OPENSTACK_TESTLAB, resourceId, monitorResourceType);
            try {
                if (Objects.nonNull(metric) && !CollectionUtils.isEmpty(metric.getMetrics())) {
                    monitorMetricsStore.storeMonitorMetric(key, metric);
                    monitorMetricsStore.updateMetricsCacheTimeToLive(key);
                }
            } catch (Exception e) {
                log.error("Cache metrics data error.{}", e.getMessage());
            }
        }
    }
}
