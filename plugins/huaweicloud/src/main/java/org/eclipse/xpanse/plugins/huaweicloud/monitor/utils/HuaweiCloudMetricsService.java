/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.utils;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsRequest;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsStore;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.models.HuaweiCloudRetryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class to encapsulate all Metric related public methods for HuaweiCloud plugin.
 */
@Slf4j
@Component
public class HuaweiCloudMetricsService {

    private final HuaweiCloudMonitorClient huaweiCloudMonitorClient;

    private final ServiceMetricsStore serviceMetricsStore;

    private final HuaweiCloudDataModelConverter huaweiCloudDataModelConverter;

    private final CredentialCenter credentialCenter;

    /**
     * Constructs a HuaweiCloudMetricsService with the necessary dependencies.
     */
    @Autowired
    public HuaweiCloudMetricsService(
            HuaweiCloudMonitorClient huaweiCloudMonitorClient,
            ServiceMetricsStore serviceMetricsStore,
            HuaweiCloudDataModelConverter huaweiCloudDataModelConverter,
            CredentialCenter credentialCenter) {
        this.huaweiCloudMonitorClient = huaweiCloudMonitorClient;
        this.serviceMetricsStore = serviceMetricsStore;
        this.huaweiCloudDataModelConverter = huaweiCloudDataModelConverter;
        this.credentialCenter = credentialCenter;
    }

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsByResource(ResourceMetricsRequest resourceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                Csp.HUAWEI,
                CredentialType.VARIABLES, resourceMetricRequest.getUserId());
        MonitorResourceType monitorResourceType = resourceMetricRequest.getMonitorResourceType();
        ICredential icredential = huaweiCloudMonitorClient.getCredentialForClient(credential);
        CesClient client = huaweiCloudMonitorClient.getCesClient(icredential,
                deployResource.getProperties().get("region"));
        Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                getTargetMetricsMap(deployResource, monitorResourceType, client);
        for (Map.Entry<MonitorResourceType, MetricInfoList> entry
                : targetMetricsMap.entrySet()) {
            ShowMetricDataRequest showMetricDataRequest =
                    huaweiCloudDataModelConverter.buildShowMetricDataRequest(
                            resourceMetricRequest, entry.getValue());
            ShowMetricDataResponse showMetricDataResponse =
                    client.showMetricDataInvoker(showMetricDataRequest)
                            .retryTimes(HuaweiCloudRetryStrategy.DEFAULT_RETRY_TIMES)
                            .retryCondition((resp, ex) -> Objects.nonNull(ex)
                                    &&
                                    ServiceResponseException.class.isAssignableFrom(ex.getClass())
                                    && (((ServiceResponseException) ex).getHttpStatusCode()
                                    == HuaweiCloudRetryStrategy.ERROR_CODE_TOO_MANY_REQUESTS
                                    || ((ServiceResponseException) ex).getHttpStatusCode()
                                    == HuaweiCloudRetryStrategy.ERROR_CODE_INTERNAL_SERVER_ERROR))
                            .backoffStrategy(
                                    new HuaweiCloudRetryStrategy(
                                            HuaweiCloudRetryStrategy.DEFAULT_DELAY_MILLIS))
                            .invoke();
            Metric metric =
                    huaweiCloudDataModelConverter.convertShowMetricDataResponseToMetric(
                            deployResource, showMetricDataResponse, entry.getValue(),
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
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                Csp.HUAWEI, CredentialType.VARIABLES, serviceMetricRequest.getUserId());
        MonitorResourceType monitorResourceType = serviceMetricRequest.getMonitorResourceType();
        ICredential icredential = huaweiCloudMonitorClient.getCredentialForClient(credential);
        CesClient client = huaweiCloudMonitorClient.getCesClient(icredential,
                deployResources.get(0).getProperties().get("region"));
        Map<String, List<MetricInfoList>> deployResourceMetricInfoMap = new HashMap<>();
        for (DeployResource deployResource : deployResources) {
            Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                    getTargetMetricsMap(deployResource, monitorResourceType, client);
            List<MetricInfoList> targetMetricInfoList = targetMetricsMap.values().stream().toList();
            deployResourceMetricInfoMap.put(deployResource.getResourceId(), targetMetricInfoList);
        }
        BatchListMetricDataRequest batchListMetricDataRequest =
                huaweiCloudDataModelConverter.buildBatchListMetricDataRequest(
                        serviceMetricRequest, deployResourceMetricInfoMap);
        BatchListMetricDataResponse batchListMetricDataResponse =
                client.batchListMetricDataInvoker(batchListMetricDataRequest)
                        .retryTimes(HuaweiCloudRetryStrategy.DEFAULT_RETRY_TIMES)
                        .retryCondition((resp, ex) -> Objects.nonNull(ex)
                                && ServiceResponseException.class.isAssignableFrom(ex.getClass())
                                && (((ServiceResponseException) ex).getHttpStatusCode()
                                == HuaweiCloudRetryStrategy.ERROR_CODE_TOO_MANY_REQUESTS
                                || ((ServiceResponseException) ex).getHttpStatusCode()
                                == HuaweiCloudRetryStrategy.ERROR_CODE_INTERNAL_SERVER_ERROR))
                        .backoffStrategy(
                                new HuaweiCloudRetryStrategy(
                                        HuaweiCloudRetryStrategy.DEFAULT_DELAY_MILLIS))
                        .invoke();
        List<Metric> metrics =
                huaweiCloudDataModelConverter.convertBatchListMetricDataResponseToMetric(
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
                serviceMetricsStore.storeMonitorMetric(Csp.HUAWEI, resourceId,
                        monitorResourceType, metric);
            } else {
                Metric cacheMetric = serviceMetricsStore.getMonitorMetric(Csp.HUAWEI, resourceId,
                        monitorResourceType);
                if (Objects.nonNull(cacheMetric)
                        && !CollectionUtils.isEmpty(cacheMetric.getMetrics())) {
                    metric = cacheMetric;
                }
            }
        }
    }

    private void doCacheActionForServiceMetrics(ServiceMetricsRequest serviceMetricRequest,
                                                Map<String, List<MetricInfoList>> resourceMetricMap,
                                                List<Metric> metrics) {
        if (serviceMetricRequest.isOnlyLastKnownMetric()) {
            for (Map.Entry<String, List<MetricInfoList>> entry : resourceMetricMap.entrySet()) {
                String resourceId = entry.getKey();
                for (MetricInfoList metricInfo : entry.getValue()) {
                    MonitorResourceType type =
                            huaweiCloudDataModelConverter.getMonitorResourceTypeByMetricName(
                                    metricInfo.getMetricName());
                    if (CollectionUtils.isEmpty(metrics)) {
                        Metric metricCache =
                                serviceMetricsStore.getMonitorMetric(Csp.HUAWEI,
                                        resourceId, type);
                        metrics.add(metricCache);
                    } else {
                        Optional<Metric> metricOptional = metrics.stream().filter(
                                metric -> Objects.nonNull(metric)
                                        && StringUtils.equals(metric.getName(),
                                        metricInfo.getMetricName())
                                        && !CollectionUtils.isEmpty(metric.getMetrics())
                                        && StringUtils.equals(resourceId,
                                        metric.getLabels().get("id"))
                        ).findAny();
                        if (metricOptional.isPresent()) {
                            serviceMetricsStore.storeMonitorMetric(Csp.HUAWEI, resourceId,
                                    type,
                                    metricOptional.get());
                        } else {
                            Metric metricCache =
                                    serviceMetricsStore.getMonitorMetric(Csp.HUAWEI,
                                            resourceId, type);
                            metrics.add(metricCache);
                        }
                    }
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
        ListMetricsResponse listMetricsResponse = client.listMetricsInvoker(request)
                .retryTimes(HuaweiCloudRetryStrategy.DEFAULT_RETRY_TIMES)
                .retryCondition((resp, ex) -> Objects.nonNull(ex)
                        && ServiceResponseException.class.isAssignableFrom(ex.getClass())
                        && (((ServiceResponseException) ex).getHttpStatusCode()
                        == HuaweiCloudRetryStrategy.ERROR_CODE_TOO_MANY_REQUESTS
                        || ((ServiceResponseException) ex).getHttpStatusCode()
                        == HuaweiCloudRetryStrategy.ERROR_CODE_INTERNAL_SERVER_ERROR))
                .backoffStrategy(
                        new HuaweiCloudRetryStrategy(HuaweiCloudRetryStrategy.DEFAULT_DELAY_MILLIS))
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
                                type.toValue(), deployResource.getResourceId());
                    }
                }
            } else {
                MetricInfoList targetMetricInfo =
                        huaweiCloudDataModelConverter.getTargetMetricInfo(metricInfoLists,
                                monitorResourceType);
                if (Objects.nonNull(targetMetricInfo)) {
                    targetMetricsMap.put(monitorResourceType, targetMetricInfo);
                } else {
                    log.error(
                            "Could not get metrics of the resource. metricType:{}, resourceId:{}",
                            monitorResourceType.toValue(), deployResource.getResourceId());
                }
            }
        }
        return targetMetricsMap;
    }

}
