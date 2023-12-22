/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor;

import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.DEFAULT_DELAY_MILLIS;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.DEFAULT_RETRY_TIMES;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsRequest;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.auth.ICredential;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.MetricsDataNotYetAvailableException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsStore;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class to encapsulate all Metric related public methods for FlexibleEngine plugin.
 */
@Slf4j
@Component
public class FlexibleEngineMetricsService {

    private final FlexibleEngineClient flexibleEngineClient;

    private final ServiceMetricsStore serviceMetricsStore;

    private final FlexibleEngineDataModelConverter modelConverter;

    private final CredentialCenter credentialCenter;

    /**
     * Constructs a FlexibleEngineMetricsService with the necessary dependencies.
     */
    @Autowired
    public FlexibleEngineMetricsService(FlexibleEngineClient flexibleEngineClient,
                                        ServiceMetricsStore serviceMetricsStore,
                                        FlexibleEngineDataModelConverter modelConverter,
                                        CredentialCenter credentialCenter) {
        this.flexibleEngineClient = flexibleEngineClient;
        this.serviceMetricsStore = serviceMetricsStore;
        this.modelConverter = modelConverter;
        this.credentialCenter = credentialCenter;
    }

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsByResource(ResourceMetricsRequest resourceMetricRequest) {
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        String regionName = deployResource.getProperties().get("region");
        if (StringUtils.isEmpty(regionName)) {
            String errorMsg = "Could not get region from resource.";
            throw new ClientApiCallFailedException(errorMsg);
        }
        List<Metric> metrics = new ArrayList<>();

        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES,
                        resourceMetricRequest.getUserId());
        ICredential icredential = flexibleEngineClient.getCredential(credential);
        CesClient client = flexibleEngineClient.getCesClient(icredential, regionName);

        MonitorResourceType monitorResourceType = resourceMetricRequest.getMonitorResourceType();
        Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                getTargetMetricsMap(deployResource, monitorResourceType, client);
        for (Map.Entry<MonitorResourceType, MetricInfoList> entry : targetMetricsMap.entrySet()) {
            ShowMetricDataRequest showMetricDataRequest =
                    modelConverter.buildShowMetricDataRequest(resourceMetricRequest,
                            entry.getValue());
            ShowMetricDataResponse showMetricDataResponse =
                    client.showMetricDataInvoker(showMetricDataRequest)
                            .retryCondition(flexibleEngineClient::matchRetryCondition)
                            .backoffStrategy(new FlexibleEngineRetryStrategy(DEFAULT_DELAY_MILLIS))
                            .invoke();
            Metric metric = modelConverter.convertShowMetricDataResponseToMetric(deployResource,
                    showMetricDataResponse, entry.getValue(),
                    resourceMetricRequest.isOnlyLastKnownMetric());
            doCacheActionForResourceMetrics(resourceMetricRequest, entry.getKey(), metric);
            metrics.add(metric);
        }
        return metrics;
    }


    /**
     * Get metrics of the @deployService.
     *
     * @param serviceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsByService(ServiceMetricsRequest serviceMetricRequest) {
        List<DeployResource> deployResources = serviceMetricRequest.getDeployResources();
        String regionName = deployResources.get(0).getProperties().get("region");
        if (StringUtils.isEmpty(regionName)) {
            String errorMsg = "Could not get region from services.";
            throw new ClientApiCallFailedException(errorMsg);
        }
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES,
                        serviceMetricRequest.getUserId());
        ICredential icredential = flexibleEngineClient.getCredential(credential);
        CesClient client = flexibleEngineClient.getCesClient(icredential, regionName);

        MonitorResourceType monitorResourceType = serviceMetricRequest.getMonitorResourceType();
        Map<String, List<MetricInfoList>> deployResourceMetricInfoMap = new HashMap<>();
        for (DeployResource deployResource : deployResources) {
            Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                    getTargetMetricsMap(deployResource, monitorResourceType, client);
            List<MetricInfoList> targetMetricInfoList = targetMetricsMap.values().stream().toList();
            deployResourceMetricInfoMap.put(deployResource.getResourceId(), targetMetricInfoList);
        }
        BatchListMetricDataRequest batchListMetricDataRequest =
                modelConverter.buildBatchListMetricDataRequest(serviceMetricRequest,
                        deployResourceMetricInfoMap);
        BatchListMetricDataResponse batchListMetricDataResponse =
                client.batchListMetricDataInvoker(batchListMetricDataRequest)
                        .retryTimes(DEFAULT_RETRY_TIMES)
                        .retryCondition(flexibleEngineClient::matchRetryCondition)
                        .backoffStrategy(new FlexibleEngineRetryStrategy(DEFAULT_DELAY_MILLIS))
                        .invoke();
        List<Metric> metrics = modelConverter.convertBatchListMetricDataResponseToMetric(
                batchListMetricDataResponse, deployResourceMetricInfoMap, deployResources,
                serviceMetricRequest.isOnlyLastKnownMetric());
        doCacheActionForServiceMetrics(serviceMetricRequest, deployResourceMetricInfoMap, metrics);
        return metrics;
    }

    private void doCacheActionForResourceMetrics(ResourceMetricsRequest resourceMetricRequest,
                                                 MonitorResourceType monitorResourceType,
                                                 Metric metric) {
        if (resourceMetricRequest.isOnlyLastKnownMetric()) {
            String resourceId = resourceMetricRequest.getDeployResource().getResourceId();
            if (Objects.nonNull(metric) && !CollectionUtils.isEmpty(metric.getMetrics())) {
                serviceMetricsStore.storeMonitorMetric(Csp.FLEXIBLE_ENGINE, resourceId,
                        monitorResourceType, metric);
            } else {
                Metric cacheMetric =
                        serviceMetricsStore.getMonitorMetric(Csp.FLEXIBLE_ENGINE, resourceId,
                                monitorResourceType);
                if (Objects.nonNull(cacheMetric) && !CollectionUtils.isEmpty(
                        cacheMetric.getMetrics())) {
                    metric = cacheMetric;
                }
            }
        }
    }

    private void doCacheActionForServiceMetrics(ServiceMetricsRequest serviceMetricRequest,
                                                Map<String, List<MetricInfoList>> resourceMetricMap,
                                                List<Metric> metrics) {
        if (serviceMetricRequest.isOnlyLastKnownMetric()) {
            if (metrics.isEmpty()) {
                fetchAndAddMetricsFromCache(resourceMetricMap, metrics);
            } else {
                updateMetricsFromCache(resourceMetricMap, metrics);
            }
        }
    }

    private void fetchAndAddMetricsFromCache(Map<String, List<MetricInfoList>> resourceMetricMap,
                                             List<Metric> metrics) {
        Map<String, Metric> metricCacheMap = new HashMap<>();
        for (Map.Entry<String, List<MetricInfoList>> entry : resourceMetricMap.entrySet()) {
            String resourceId = entry.getKey();
            for (MetricInfoList metricInfo : entry.getValue()) {
                MonitorResourceType type = modelConverter.getMonitorResourceTypeByMetricName(
                        metricInfo.getMetricName());
                Metric metricCache =
                        serviceMetricsStore.getMonitorMetric(Csp.FLEXIBLE_ENGINE, resourceId, type);
                if (Objects.nonNull(metricCache)) {
                    metricCacheMap.put(metricInfo.getMetricName(), metricCache);
                }
            }
        }
        if (!CollectionUtils.isEmpty(metricCacheMap)) {
            metrics.addAll(metricCacheMap.values());
        } else {
            log.error("No monitor metrics available for the service, "
                    + "Please wait for 3-5 minutes and try again.");
            throw new MetricsDataNotYetAvailableException(
                    "No monitor metrics available for the service, "
                            + "Please wait for 3-5 minutes and try again.");
        }
    }

    private void updateMetricsFromCache(Map<String, List<MetricInfoList>> resourceMetricMap,
                                        List<Metric> metrics) {
        for (Map.Entry<String, List<MetricInfoList>> entry : resourceMetricMap.entrySet()) {
            String resourceId = entry.getKey();
            for (MetricInfoList metricInfo : entry.getValue()) {
                MonitorResourceType type = modelConverter.getMonitorResourceTypeByMetricName(
                        metricInfo.getMetricName());
                Metric metric = metrics.stream()
                        .filter(m -> Objects.nonNull(m) && StringUtils.equals(m.getName(),
                                metricInfo.getMetricName()) && !CollectionUtils.isEmpty(
                                m.getMetrics()) && StringUtils.equals(resourceId,
                                m.getLabels().get("id"))).findAny().orElse(null);
                if (metric == null) {
                    Metric metricCache =
                            serviceMetricsStore.getMonitorMetric(Csp.FLEXIBLE_ENGINE, resourceId,
                                    type);
                    if (Objects.nonNull(metricCache)) {
                        metrics.add(metricCache);
                    }
                } else {
                    serviceMetricsStore.storeMonitorMetric(Csp.FLEXIBLE_ENGINE, resourceId, type,
                            metric);
                }
            }
        }
    }

    private Map<MonitorResourceType, MetricInfoList> getTargetMetricsMap(
            DeployResource deployResource, MonitorResourceType monitorResourceType,
            CesClient client) {
        Map<MonitorResourceType, MetricInfoList> targetMetricsMap = new HashMap<>();
        ListMetricsRequest request = modelConverter.buildListMetricsRequest(deployResource);
        log.error("Flexible Request:{}", request);
        ListMetricsResponse listMetricsResponse =
                client.listMetricsInvoker(request).retryTimes(DEFAULT_RETRY_TIMES)
                        .retryCondition(flexibleEngineClient::matchRetryCondition)
                        .backoffStrategy(new FlexibleEngineRetryStrategy(DEFAULT_DELAY_MILLIS))
                        .invoke();
        if (Objects.nonNull(listMetricsResponse) && !CollectionUtils.isEmpty(
                listMetricsResponse.getMetrics())) {
            List<MetricInfoList> metricInfoLists = listMetricsResponse.getMetrics();
            if (Objects.isNull(monitorResourceType)) {
                for (MonitorResourceType type : MonitorResourceType.values()) {
                    MetricInfoList targetMetricInfo =
                            modelConverter.getTargetMetricInfo(metricInfoLists, type);
                    if (Objects.nonNull(targetMetricInfo)) {
                        targetMetricsMap.put(type, targetMetricInfo);
                    } else {
                        log.error("Could not get metrics of the resource. metricType:{},"
                                + "resourceId:{}", type.toValue(), deployResource.getResourceId());
                    }
                }
            } else {
                MetricInfoList targetMetricInfo =
                        modelConverter.getTargetMetricInfo(metricInfoLists, monitorResourceType);
                if (Objects.nonNull(targetMetricInfo)) {
                    targetMetricsMap.put(monitorResourceType, targetMetricInfo);
                } else {
                    log.error("Could not get metrics of the resource. metricType:{}, resourceId:{}",
                            monitorResourceType.toValue(), deployResource.getResourceId());
                }
            }
            return targetMetricsMap;
        } else {
            log.error("No monitor metrics available for the service, "
                    + "Please wait for 3-5 minutes and try again.");
            throw new MetricsDataNotYetAvailableException(
                    "No monitor metrics available for the service, "
                            + "Please wait for 3-5 minutes and try again.");
        }
    }


}
