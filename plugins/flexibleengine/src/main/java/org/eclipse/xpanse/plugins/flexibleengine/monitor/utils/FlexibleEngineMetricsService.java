/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.internal.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.core.internal.model.Project;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpRequestBase;
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
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineMonitorClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class to encapsulate all Metric related public methods for FlexibleEngine plugin.
 */
@Slf4j
@Component
public class FlexibleEngineMetricsService {


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FlexibleEngineMetricsConverter flexibleEngineMetricsConverter;

    private final FlexibleEngineMonitorClient flexibleEngineMonitorClient;

    private final ServiceMetricsStore serviceMetricsStore;

    private final RetryTemplateService retryTemplateService;

    private final CredentialCenter credentialCenter;

    /**
     * Constructor for the MetricsService class.
     *
     * @param flexibleEngineMonitorClient    instance of FlexibleEngineMonitorClient
     * @param flexibleEngineMetricsConverter instance of FlexibleEngineMonitorConverter
     * @param serviceMetricsStore            instance of MetricItemsStore
     * @param retryTemplateService           instance of retryTemplateService
     * @param credentialCenter               instance of CredentialCenter
     */
    @Autowired
    public FlexibleEngineMetricsService(
            FlexibleEngineMonitorClient flexibleEngineMonitorClient,
            FlexibleEngineMetricsConverter flexibleEngineMetricsConverter,
            ServiceMetricsStore serviceMetricsStore,
            RetryTemplateService retryTemplateService,
            CredentialCenter credentialCenter) {
        this.flexibleEngineMetricsConverter = flexibleEngineMetricsConverter;
        this.flexibleEngineMonitorClient = flexibleEngineMonitorClient;
        this.serviceMetricsStore = serviceMetricsStore;
        this.retryTemplateService = retryTemplateService;
        this.credentialCenter = credentialCenter;
    }


    private Project queryProjectInfo(AbstractCredentialInfo credential, String url) {
        try {
            HttpRequestBase requestBase =
                    flexibleEngineMonitorClient.buildGetRequest(credential, url);
            KeystoneListProjectsResponse projectsResponse =
                    retryTemplateService.queryProjectInfo(requestBase);
            if (Objects.nonNull(projectsResponse)
                    && !CollectionUtils.isEmpty(projectsResponse.getProjects())) {
                return projectsResponse.getProjects().get(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            String errorMsg = String.format("Query project info by FlexibleEngine Client error. %s",
                    e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    private ShowMetricDataResponse queryMetricData(AbstractCredentialInfo credential, String url) {
        try {
            HttpRequestBase requestBase =
                    flexibleEngineMonitorClient.buildGetRequest(credential, url);
            return retryTemplateService.queryMetricData(requestBase);
        } catch (Exception e) {
            String errorMsg = String.format("Query metric data by FlexibleEngine Client error. %s",
                    e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    private ListMetricsResponse queryMetricItemList(AbstractCredentialInfo credential, String url) {
        try {
            HttpRequestBase requestBase =
                    flexibleEngineMonitorClient.buildGetRequest(credential, url);
            return retryTemplateService.queryMetricItemList(requestBase);
        } catch (Exception e) {
            String errorMsg =
                    String.format("Query metric item list by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    private BatchListMetricDataResponse batchQueryMetricData(AbstractCredentialInfo credential,
                                                             BatchListMetricDataRequest request,
                                                             String url) {
        try {
            String requestBody = OBJECT_MAPPER.writeValueAsString(request.getBody());
            HttpRequestBase requestBase =
                    flexibleEngineMonitorClient.buildPostRequest(credential, url, requestBody);
            return retryTemplateService.batchQueryMetricData(requestBase, requestBody);
        } catch (Exception e) {
            String errorMsg =
                    String.format("Batch query metric data by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsForResource(ResourceMetricsRequest resourceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                Csp.FLEXIBLE_ENGINE,
                CredentialType.VARIABLES, resourceMetricRequest.getUserId());
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        String region = deployResource.getProperties().get("region");
        Project project = getProjectInfoByRegion(credential, region);

        MonitorResourceType resourceType = resourceMetricRequest.getMonitorResourceType();
        Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                getTargetMetricsMap(deployResource, credential, resourceType, project);
        for (Map.Entry<MonitorResourceType, MetricInfoList> entry
                : targetMetricsMap.entrySet()) {
            String url = flexibleEngineMetricsConverter.buildMonitorMetricUrl(
                    resourceMetricRequest, project.getId(), entry.getValue());
            if (StringUtils.isNotBlank(url)) {
                ShowMetricDataResponse response = queryMetricData(credential, url);
                Metric metric =
                        flexibleEngineMetricsConverter.convertResponseToMetric(
                                deployResource, response, entry.getValue(),
                                resourceMetricRequest.isOnlyLastKnownMetric());
                doCacheActionForResourceMetrics(resourceMetricRequest, entry.getKey(),
                        metric);
                metrics.add(metric);
            }
        }
        return metrics;
    }

    /**
     * Get metrics of the @DeployService.
     *
     * @param serviceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        List<DeployResource> deployResources = serviceMetricRequest.getDeployResources();
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES,
                serviceMetricRequest.getUserId());
        MonitorResourceType resourceType = serviceMetricRequest.getMonitorResourceType();
        String region = deployResources.get(0).getProperties().get("region");
        Project project = getProjectInfoByRegion(credential, region);
        Map<String, List<MetricInfoList>> deployResourceMetricInfoMap = new HashMap<>();
        for (DeployResource deployResource : deployResources) {
            Map<MonitorResourceType, MetricInfoList> targetMetricsMap =
                    getTargetMetricsMap(deployResource, credential, resourceType, project);
            List<MetricInfoList> targetMetricInfoList = targetMetricsMap.values().stream().toList();
            deployResourceMetricInfoMap.put(deployResource.getResourceId(),
                    targetMetricInfoList);
        }
        BatchListMetricDataRequest batchListMetricDataRequest =
                FlexibleEngineMetricsConverter.buildBatchListMetricDataRequest(
                        serviceMetricRequest, deployResourceMetricInfoMap);

        String url = FlexibleEngineMetricsConverter.getBatchQueryMetricBasicUrl(region,
                project.getId()).toString();

        BatchListMetricDataResponse batchListMetricDataResponse =
                batchQueryMetricData(credential, batchListMetricDataRequest, url);

        List<Metric> metrics =
                flexibleEngineMetricsConverter.convertBatchListMetricDataResponseToMetric(
                        batchListMetricDataResponse, deployResourceMetricInfoMap, deployResources,
                        serviceMetricRequest.isOnlyLastKnownMetric());
        doCacheActionForServiceMetrics(serviceMetricRequest, deployResourceMetricInfoMap, metrics);
        return metrics;
    }


    private Project getProjectInfoByRegion(AbstractCredentialInfo credential, String region) {

        Project project = null;
        if (StringUtils.isNotBlank(region)) {
            String projectQueryUrl =
                    flexibleEngineMetricsConverter.buildProjectQueryUrl(region).toString();
            project =
                    queryProjectInfo(credential, projectQueryUrl);
        }
        if (Objects.isNull(project) || StringUtils.isBlank(project.getId())) {
            throw new ClientApiCallFailedException(
                    "Query project info by FlexibleEngine Client failed. Project info is null.");
        }
        return project;
    }


    private void doCacheActionForResourceMetrics(ResourceMetricsRequest resourceMetricRequest,
                                                 MonitorResourceType monitorResourceType,
                                                 Metric metric) {
        if (resourceMetricRequest.isOnlyLastKnownMetric()) {
            String resourceId = resourceMetricRequest.getDeployResource().getResourceId();
            if (Objects.nonNull(metric) && !CollectionUtils.isEmpty(metric.getMetrics())) {
                serviceMetricsStore.storeMonitorMetric(Csp.FLEXIBLE_ENGINE,
                        resourceId, monitorResourceType, metric);

            } else {
                Metric cacheMetric =
                        serviceMetricsStore.getMonitorMetric(Csp.FLEXIBLE_ENGINE, resourceId,
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
                MonitorResourceType type =
                        flexibleEngineMetricsConverter.getMonitorResourceTypeByMetricName(
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
                MonitorResourceType type =
                        flexibleEngineMetricsConverter.getMonitorResourceTypeByMetricName(
                                metricInfo.getMetricName());
                Metric metric = metrics.stream()
                        .filter(m -> Objects.nonNull(m)
                                && StringUtils.equals(m.getName(), metricInfo.getMetricName())
                                && !CollectionUtils.isEmpty(m.getMetrics())
                                && StringUtils.equals(resourceId, m.getLabels().get("id")))
                        .findAny()
                        .orElse(null);
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
            DeployResource deployResource,
            AbstractCredentialInfo credential,
            MonitorResourceType monitorResourceType,
            Project project) {

        Map<MonitorResourceType, MetricInfoList> targetMetricsMap = new HashMap<>();
        String url =
                flexibleEngineMetricsConverter.buildListMetricsUrl(deployResource,
                        project.getId());
        ListMetricsResponse listMetricsResponse = queryMetricItemList(credential, url);
        if (Objects.nonNull(listMetricsResponse)
                && !CollectionUtils.isEmpty(listMetricsResponse.getMetrics())) {
            List<MetricInfoList> metrics = listMetricsResponse.getMetrics();
            if (Objects.isNull(monitorResourceType)) {
                for (MonitorResourceType type : MonitorResourceType.values()) {
                    MetricInfoList targetMetricInfo =
                            flexibleEngineMetricsConverter.getTargetMetricInfo(metrics, type);
                    if (Objects.nonNull(targetMetricInfo)) {
                        targetMetricsMap.put(type, targetMetricInfo);
                    } else {
                        log.error("Could not get metric item of the resource. metricType:{},"
                                + "resourceId:{}", type.toValue(), deployResource.getResourceId());
                    }
                }
            } else {
                MetricInfoList targetMetricInfo =
                        flexibleEngineMetricsConverter.getTargetMetricInfo(metrics,
                                monitorResourceType);
                if (Objects.nonNull(targetMetricInfo)) {
                    targetMetricsMap.put(monitorResourceType, targetMetricInfo);
                } else {
                    log.error(
                            "Could not get metrics of the resource. metricType:{}, resourceId:{}",
                            monitorResourceType.toValue(), deployResource.getResourceId());
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
