/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsRequest;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.auth.ICredential;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.cache.monitor.MonitorMetricsCacheKey;
import org.eclipse.xpanse.modules.cache.monitor.MonitorMetricsStore;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.MetricsDataNotYetAvailableException;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Class to encapsulate all Metric related public methods for HuaweiCloud plugin. */
@Slf4j
@Component
public class HuaweiCloudMetricsService {

    @Resource private HuaweiCloudClient huaweiCloudClient;
    @Resource private MonitorMetricsStore monitorMetricsStore;
    @Resource private HuaweiCloudDataModelConverter huaweiCloudDataModelConverter;
    @Resource private HuaweiCloudRetryStrategy huaweiCloudRetryStrategy;

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    @Retryable(
            retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<Metric> getMetricsByResource(ResourceMetricsRequest resourceMetricRequest) {
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        String siteName = resourceMetricRequest.getRegion().getSite();
        String regionName = resourceMetricRequest.getRegion().getName();
        String userId = resourceMetricRequest.getUserId();
        List<Metric> metrics = new ArrayList<>();
        try {
            MonitorResourceType monitorResourceType =
                    resourceMetricRequest.getMonitorResourceType();
            ICredential icredential =
                    huaweiCloudClient.getBasicCredential(siteName, regionName, userId);
            CesClient client = huaweiCloudClient.getCesClient(icredential, regionName);
            Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                    getTargetMetricsMap(deployResource, monitorResourceType, client);
            for (Map.Entry<MonitorResourceType, MetricInfoList> entry :
                    targetMetricsMap.entrySet()) {
                ShowMetricDataRequest showMetricDataRequest =
                        huaweiCloudDataModelConverter.buildShowMetricDataRequest(
                                resourceMetricRequest, entry.getValue());
                ShowMetricDataResponse showMetricDataResponse =
                        client.showMetricDataInvoker(showMetricDataRequest)
                                .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                                .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                                .backoffStrategy(huaweiCloudRetryStrategy)
                                .invoke();
                Metric metric =
                        huaweiCloudDataModelConverter.convertShowMetricDataResponseToMetric(
                                deployResource,
                                showMetricDataResponse,
                                entry.getValue(),
                                resourceMetricRequest.isOnlyLastKnownMetric());
                doCacheActionForResourceMetrics(resourceMetricRequest, entry.getKey(), metric);
                metrics.add(metric);
            }
            return metrics;
        } catch (Exception e) {
            log.error("Get metrics of resource {} failed.", deployResource.getResourceId());
            huaweiCloudRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    /**
     * Get metrics of the @deployService.
     *
     * @param serviceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    @Retryable(
            retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<Metric> getMetricsByService(ServiceMetricsRequest serviceMetricRequest) {
        List<DeployResource> deployResources = serviceMetricRequest.getDeployResources();

        String siteName = serviceMetricRequest.getRegion().getSite();
        String regionName = serviceMetricRequest.getRegion().getName();
        String userId = serviceMetricRequest.getUserId();
        try {
            MonitorResourceType monitorResourceType = serviceMetricRequest.getMonitorResourceType();
            ICredential icredential =
                    huaweiCloudClient.getBasicCredential(siteName, regionName, userId);
            CesClient client = huaweiCloudClient.getCesClient(icredential, regionName);
            Map<String, List<MetricInfoList>> deployResourceMetricInfoMap = new HashMap<>();
            for (DeployResource deployResource : deployResources) {
                Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                        getTargetMetricsMap(deployResource, monitorResourceType, client);
                List<MetricInfoList> targetMetricInfoList =
                        targetMetricsMap.values().stream().toList();
                deployResourceMetricInfoMap.put(
                        deployResource.getResourceId(), targetMetricInfoList);
            }
            BatchListMetricDataRequest batchListMetricDataRequest =
                    huaweiCloudDataModelConverter.buildBatchListMetricDataRequest(
                            serviceMetricRequest, deployResourceMetricInfoMap);
            BatchListMetricDataResponse batchListMetricDataResponse =
                    client.batchListMetricDataInvoker(batchListMetricDataRequest)
                            .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                            .backoffStrategy(huaweiCloudRetryStrategy)
                            .invoke();
            List<Metric> metrics =
                    huaweiCloudDataModelConverter.convertBatchListMetricDataResponseToMetric(
                            batchListMetricDataResponse,
                            deployResourceMetricInfoMap,
                            deployResources,
                            serviceMetricRequest.isOnlyLastKnownMetric());
            doCacheActionForServiceMetrics(
                    serviceMetricRequest, deployResourceMetricInfoMap, metrics);
            return metrics;
        } catch (Exception e) {
            log.error("Get metrics of service {} failed.", serviceMetricRequest.getServiceId());
            huaweiCloudRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    private void doCacheActionForResourceMetrics(
            ResourceMetricsRequest resourceMetricRequest,
            MonitorResourceType monitorResourceType,
            Metric metric) {
        if (resourceMetricRequest.isOnlyLastKnownMetric()) {
            String resourceId = resourceMetricRequest.getDeployResource().getResourceId();
            MonitorMetricsCacheKey cacheKey =
                    new MonitorMetricsCacheKey(Csp.HUAWEI_CLOUD, resourceId, monitorResourceType);
            try {
                if (Objects.nonNull(metric) && !CollectionUtils.isEmpty(metric.getMetrics())) {
                    monitorMetricsStore.storeMonitorMetric(cacheKey, metric);
                    monitorMetricsStore.updateMetricsCacheTimeToLive(cacheKey);
                } else {
                    Metric cacheMetric = monitorMetricsStore.getMonitorMetric(cacheKey);
                    monitorMetricsStore.updateMetricsCacheTimeToLive(cacheKey);
                    if (Objects.nonNull(cacheMetric)
                            && !CollectionUtils.isEmpty(cacheMetric.getMetrics())) {
                        metric = cacheMetric;
                    }
                }
            } catch (Exception e) {
                log.error("Update metrics cache data error.{}", e.getMessage());
            }
        }
    }

    private void doCacheActionForServiceMetrics(
            ServiceMetricsRequest serviceMetricRequest,
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

    private void fetchAndAddMetricsFromCache(
            Map<String, List<MetricInfoList>> resourceMetricMap, List<Metric> metrics) {
        Map<String, Metric> metricCacheMap = new HashMap<>();
        for (Map.Entry<String, List<MetricInfoList>> entry : resourceMetricMap.entrySet()) {
            String resourceId = entry.getKey();
            for (MetricInfoList metricInfo : entry.getValue()) {
                MonitorResourceType type =
                        huaweiCloudDataModelConverter.getMonitorResourceTypeByMetricName(
                                metricInfo.getMetricName());
                MonitorMetricsCacheKey cacheKey =
                        new MonitorMetricsCacheKey(Csp.HUAWEI_CLOUD, resourceId, type);
                try {
                    Metric metricCache = monitorMetricsStore.getMonitorMetric(cacheKey);
                    monitorMetricsStore.updateMetricsCacheTimeToLive(cacheKey);
                    if (Objects.nonNull(metricCache)) {
                        metricCacheMap.put(metricInfo.getMetricName(), metricCache);
                    }
                } catch (Exception e) {
                    log.error("Get metrics cache data error.{}", e.getMessage());
                }
            }
        }
        if (!CollectionUtils.isEmpty(metricCacheMap)) {
            metrics.addAll(metricCacheMap.values());
        } else {
            log.error(
                    "No monitor metrics available for the service, "
                            + "Please wait for 3-5 minutes and try again.");
            throw new MetricsDataNotYetAvailableException(
                    "No monitor metrics available for the service, "
                            + "Please wait for 3-5 minutes and try again.");
        }
    }

    private void updateMetricsFromCache(
            Map<String, List<MetricInfoList>> resourceMetricMap, List<Metric> metrics) {
        for (Map.Entry<String, List<MetricInfoList>> entry : resourceMetricMap.entrySet()) {
            String resourceId = entry.getKey();
            for (MetricInfoList metricInfo : entry.getValue()) {
                MonitorResourceType type =
                        huaweiCloudDataModelConverter.getMonitorResourceTypeByMetricName(
                                metricInfo.getMetricName());
                MonitorMetricsCacheKey cacheKey =
                        new MonitorMetricsCacheKey(Csp.HUAWEI_CLOUD, resourceId, type);
                Metric metric =
                        metrics.stream()
                                .filter(
                                        m ->
                                                Objects.nonNull(m)
                                                        && StringUtils.equals(
                                                                m.getName(),
                                                                metricInfo.getMetricName())
                                                        && !CollectionUtils.isEmpty(m.getMetrics())
                                                        && StringUtils.equals(
                                                                resourceId,
                                                                m.getLabels().get("id")))
                                .findAny()
                                .orElse(null);
                try {
                    if (Objects.isNull(metric)) {
                        Metric metricCache = monitorMetricsStore.getMonitorMetric(cacheKey);
                        monitorMetricsStore.updateMetricsCacheTimeToLive(cacheKey);
                        if (Objects.nonNull(metricCache)) {
                            metrics.add(metricCache);
                        }
                    } else {
                        monitorMetricsStore.storeMonitorMetric(cacheKey, metric);
                        monitorMetricsStore.updateMetricsCacheTimeToLive(cacheKey);
                    }
                } catch (Exception e) {
                    log.error("Update metrics cache data error.{}", e.getMessage());
                }
            }
        }
    }

    private Map<MonitorResourceType, MetricInfoList> getTargetMetricsMap(
            DeployResource deployResource,
            MonitorResourceType monitorResourceType,
            CesClient client) {
        Map<MonitorResourceType, MetricInfoList> targetMetricsMap = new HashMap<>();
        ListMetricsRequest request =
                huaweiCloudDataModelConverter.buildListMetricsRequest(deployResource);
        ListMetricsResponse listMetricsResponse =
                client.listMetricsInvoker(request)
                        .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                        .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                        .backoffStrategy(huaweiCloudRetryStrategy)
                        .invoke();
        if (Objects.nonNull(listMetricsResponse)
                && !CollectionUtils.isEmpty(listMetricsResponse.getMetrics())) {
            List<MetricInfoList> metricInfoLists = listMetricsResponse.getMetrics();
            if (Objects.isNull(monitorResourceType)) {
                for (MonitorResourceType type : MonitorResourceType.values()) {
                    MetricInfoList targetMetricInfo =
                            huaweiCloudDataModelConverter.getTargetMetricInfo(
                                    metricInfoLists, type);
                    if (Objects.nonNull(targetMetricInfo)) {
                        targetMetricsMap.put(type, targetMetricInfo);
                    } else {
                        log.error(
                                "Could not get metrics of the resource. metricType:{},"
                                        + "resourceId:{}",
                                type.toValue(),
                                deployResource.getResourceId());
                    }
                }
            } else {
                MetricInfoList targetMetricInfo =
                        huaweiCloudDataModelConverter.getTargetMetricInfo(
                                metricInfoLists, monitorResourceType);
                if (Objects.nonNull(targetMetricInfo)) {
                    targetMetricsMap.put(monitorResourceType, targetMetricInfo);
                } else {
                    log.error(
                            "Could not get metrics of the resource. metricType:{}, resourceId:{}",
                            monitorResourceType.toValue(),
                            deployResource.getResourceId());
                }
            }
            return targetMetricsMap;
        } else {
            log.error(
                    "No monitor metrics available for the service, "
                            + "Please wait for 3-5 minutes and try again.");
            throw new MetricsDataNotYetAvailableException(
                    "No monitor metrics available for the service, "
                            + "Please wait for 3-5 minutes and try again.");
        }
    }
}
