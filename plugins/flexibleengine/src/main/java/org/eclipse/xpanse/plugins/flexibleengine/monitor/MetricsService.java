/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor;


import com.cloud.apigateway.sdk.utils.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
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
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCalledException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineMonitorMetrics;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineNameSpaceKind;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMonitorCache;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMonitorConverter;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.RetryTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class to encapsulate all Metric related public methods for FlexibleEngine plugin.
 */
@Slf4j
@Component
public class MetricsService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final FlexibleEngineMonitorConverter flexibleEngineMonitorConverter;

    private final FlexibleEngineMonitorCache flexibleEngineMonitorCache;

    private final RetryTemplateService retryTemplateService;

    private final CredentialCenter credentialCenter;

    /**
     * Constructor for the MetricsService class.
     *
     * @param flexibleEngineMonitorConverter instance of FlexibleEngineMonitorConverter
     * @param flexibleEngineMonitorCache     instance of FlexibleEngineMonitorCache
     * @param retryTemplateService           instance of retryTemplateService
     * @param credentialCenter               instance of CredentialCenter
     */
    @Autowired
    public MetricsService(
            FlexibleEngineMonitorConverter flexibleEngineMonitorConverter,
            FlexibleEngineMonitorCache flexibleEngineMonitorCache,
            RetryTemplateService retryTemplateService,
            CredentialCenter credentialCenter) {
        this.flexibleEngineMonitorConverter = flexibleEngineMonitorConverter;
        this.flexibleEngineMonitorCache = flexibleEngineMonitorCache;
        this.retryTemplateService = retryTemplateService;
        this.credentialCenter = credentialCenter;
    }

    /**
     * Get HttpRequestBase of the Get request of the FlexibleEngine API.
     *
     * @param credential The credential of the service.
     * @param url        The request url of the FlexibleEngine API.
     * @return Returns HttpRequestBase
     */
    public HttpRequestBase buildGetRequest(AbstractCredentialInfo credential, String url)
            throws Exception {
        String accessKey = null;
        String securityKey = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = ((CredentialVariables) credential).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (FlexibleEngineMonitorConstants.OS_ACCESS_KEY.equals(
                        credentialVariable.getName())) {
                    accessKey = credentialVariable.getValue();
                }
                if (FlexibleEngineMonitorConstants.OS_SECRET_KEY.equals(
                        credentialVariable.getName())) {
                    securityKey = credentialVariable.getValue();
                }
            }
        }
        return Client.get(accessKey, securityKey, url,
                flexibleEngineMonitorConverter.getHeaders());
    }

    /**
     * Get HttpRequestBase of the Get request of the FlexibleEngine API.
     *
     * @param credential The credential of the service.
     * @param url        The request url of the FlexibleEngine API.
     * @return Returns HttpRequestBase
     */
    public HttpRequestBase buildPostRequest(AbstractCredentialInfo credential, String url,
                                            String postBody) throws Exception {
        String accessKey = null;
        String securityKey = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = ((CredentialVariables) credential).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (FlexibleEngineMonitorConstants.OS_ACCESS_KEY.equals(
                        credentialVariable.getName())) {
                    accessKey = credentialVariable.getValue();
                }
                if (FlexibleEngineMonitorConstants.OS_SECRET_KEY.equals(
                        credentialVariable.getName())) {
                    securityKey = credentialVariable.getValue();
                }
            }
        }
        return Client.post(accessKey, securityKey, url,
                flexibleEngineMonitorConverter.getHeaders(), postBody);
    }

    private Project queryProjectInfo(AbstractCredentialInfo credential, String url) {
        try {
            HttpRequestBase requestBase = buildGetRequest(credential, url);
            return retryTemplateService.queryProjectInfo(requestBase);
        } catch (Exception e) {
            String errorMsg = String.format("Query project info by FlexibleEngine Client error. %s",
                    e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCalledException(errorMsg);
        }
    }

    private ShowMetricDataResponse queryMetricData(AbstractCredentialInfo credential, String url) {
        try {
            HttpRequestBase requestBase = buildGetRequest(credential, url);
            return retryTemplateService.queryMetricData(requestBase);
        } catch (Exception e) {
            String errorMsg = String.format("Query metric data by FlexibleEngine Client error. %s",
                    e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCalledException(errorMsg);
        }
    }

    private ListMetricsResponse queryMetricItemList(AbstractCredentialInfo credential, String url) {
        try {
            HttpRequestBase requestBase = buildGetRequest(credential, url);
            return retryTemplateService.queryMetricItemList(requestBase);
        } catch (Exception e) {
            String errorMsg =
                    String.format("Query metric item list by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCalledException(errorMsg);
        }
    }

    private BatchListMetricDataResponse batchQueryMetricData(AbstractCredentialInfo credential,
                                                             BatchListMetricDataRequest request,
                                                             String url) {
        try {
            String requestBody = objectMapper.writeValueAsString(request.getBody());
            HttpRequestBase requestBase = buildPostRequest(credential, url, requestBody);
            return retryTemplateService.batchQueryMetricData(requestBase, requestBody);
        } catch (Exception e) {
            String errorMsg =
                    String.format("Batch query metric data by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCalledException(errorMsg);
        }
    }

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsForResource(ResourceMetricRequest resourceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                Csp.FLEXIBLE_ENGINE, resourceMetricRequest.getXpanseUserName(),
                CredentialType.VARIABLES);
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        String region = deployResource.getProperties().get("region");
        MonitorResourceType resourceType = resourceMetricRequest.getMonitorResourceType();
        if (StringUtils.isNotBlank(region)) {
            Project project =
                    queryProjectInfo(credential,
                            flexibleEngineMonitorConverter.buildProjectQueryUrl(region).toString());
            if (Objects.nonNull(project) && StringUtils.isNotBlank(project.getId())) {
                List<MetricInfoList> targetMetrics =
                        getTargetMetricInfoList(deployResource, credential, resourceType, project);
                for (MetricInfoList metricInfoList : targetMetrics) {
                    String url = flexibleEngineMonitorConverter.buildMonitorMetricUrl(
                            resourceMetricRequest, project.getId(), metricInfoList);
                    if (StringUtils.isNotBlank(url)) {
                        ShowMetricDataResponse response = queryMetricData(credential, url);
                        Metric metric =
                                flexibleEngineMonitorConverter.convertResponseToMetric(
                                        deployResource, response, metricInfoList,
                                        resourceMetricRequest.isOnlyLastKnownMetric());

                        if (Objects.nonNull(metric)
                                && !CollectionUtils.isEmpty(metric.getMetrics())) {
                            metrics.add(metric);
                            flexibleEngineMonitorCache.set(deployResource.getResourceId(), metric);
                        } else {
                            List<Metric> metricCache =
                                    flexibleEngineMonitorCache.get(deployResource.getResourceId(),
                                            metricInfoList.getMetricName());
                            metrics.addAll(metricCache);
                        }
                    }
                }
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
    public List<Metric> getMetricsForService(ServiceMetricRequest serviceMetricRequest) {
        List<DeployResource> deployResources = serviceMetricRequest.getDeployResources();
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                Csp.FLEXIBLE_ENGINE, serviceMetricRequest.getXpanseUserName(),
                CredentialType.VARIABLES);
        MonitorResourceType resourceType = serviceMetricRequest.getMonitorResourceType();
        String region = deployResources.get(0).getProperties().get("region");
        Project project = null;
        if (StringUtils.isNotBlank(region)) {
            String projectQueryUrl =
                    flexibleEngineMonitorConverter.buildProjectQueryUrl(region).toString();
            project =
                    queryProjectInfo(credential, projectQueryUrl);
        }
        if (Objects.isNull(project) || StringUtils.isBlank(project.getId())) {
            throw new ClientApiCalledException(
                    "Query project info by FlexibleEngine Client failed. Project info is null.");
        }
        Map<String, List<MetricInfoList>> deployResourceMetricInfoMap = new HashMap<>();
        for (DeployResource deployResource : deployResources) {
            List<MetricInfoList> targetMetricInfoList =
                    getTargetMetricInfoList(deployResource, credential, resourceType, project);
            deployResourceMetricInfoMap.put(deployResource.getResourceId(),
                    targetMetricInfoList);
        }
        BatchListMetricDataRequest batchListMetricDataRequest =
                FlexibleEngineMonitorConverter.buildBatchListMetricDataRequest(
                        serviceMetricRequest, deployResourceMetricInfoMap);

        String url = FlexibleEngineMonitorConverter.getBatchQueryMetricBasicUrl(region,
                project.getId()).toString();

        BatchListMetricDataResponse batchListMetricDataResponse =
                batchQueryMetricData(credential, batchListMetricDataRequest, url);

        List<Metric> metrics =
                flexibleEngineMonitorConverter.convertBatchListMetricDataResponseToMetric(
                        batchListMetricDataResponse, deployResourceMetricInfoMap, deployResources,
                        serviceMetricRequest.isOnlyLastKnownMetric());
        cacheServiceMetricsData(metrics, serviceMetricRequest);

        return metrics;
    }

    private void cacheServiceMetricsData(List<Metric> metrics,
                                         ServiceMetricRequest serviceMetricRequest) {
        if (!CollectionUtils.isEmpty(metrics)) {
            for (DeployResource deployResource : serviceMetricRequest.getDeployResources()) {
                for (Metric metric : metrics) {
                    String id = metric.getLabels().get("id");
                    if (id.equals(deployResource.getResourceId())) {
                        flexibleEngineMonitorCache.set(deployResource.getResourceId(), metric);
                    }
                }
            }
        } else {
            for (DeployResource deployResource : serviceMetricRequest.getDeployResources()) {
                List<Metric> metricList =
                        flexibleEngineMonitorCache.get(deployResource.getResourceId(),
                                serviceMetricRequest.getMonitorResourceType().toValue());
                metrics.addAll(metricList);
            }
        }
    }

    private List<MetricInfoList> getTargetMetricInfoList(DeployResource deployResource,
                                                         AbstractCredentialInfo credential,
                                                         MonitorResourceType monitorResourceType,
                                                         Project project) {

        List<MetricInfoList> targetMetrics = new ArrayList<>();
        String url =
                flexibleEngineMonitorConverter.buildListMetricsUrl(deployResource,
                        project.getId());
        ListMetricsResponse listMetricsResponse = queryMetricItemList(credential, url);
        if (Objects.nonNull(listMetricsResponse)) {
            List<MetricInfoList> metrics = listMetricsResponse.getMetrics();
            if (Objects.isNull(monitorResourceType)) {
                for (MonitorResourceType type : MonitorResourceType.values()) {
                    MetricInfoList targetMetricInfo = getTargetMetricInfo(metrics, type);
                    if (Objects.nonNull(targetMetricInfo)) {
                        targetMetrics.add(targetMetricInfo);
                    } else {
                        log.error("Could not get metric item of the resource. metricType:{},"
                                + "resourceId:{}", type.toValue(), deployResource.getResourceId());
                    }
                }
            } else {
                MetricInfoList targetMetricInfo = getTargetMetricInfo(metrics, monitorResourceType);
                if (Objects.nonNull(targetMetricInfo)) {
                    targetMetrics.add(targetMetricInfo);
                } else {
                    log.error(
                            "Could not get metrics of the resource. metricType:{}, resourceId:{}",
                            monitorResourceType.toValue(), deployResource.getResourceId());
                }
            }
        }
        return targetMetrics;

    }


    private MetricInfoList getTargetMetricInfo(List<MetricInfoList> metrics,
                                               MonitorResourceType type) {
        if (MonitorResourceType.CPU.equals(type)) {
            for (MetricInfoList metric : metrics) {
                if (isAgentCpuMetrics(metric)) {
                    return metric;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(FlexibleEngineMonitorMetrics.CPU_UTILIZED);
            defaultMetricInfo.setUnit("%");
            return defaultMetricInfo;
        }
        if (MonitorResourceType.MEM.equals(type)) {
            for (MetricInfoList metric : metrics) {
                if (isAgentMemMetrics(metric)) {
                    return metric;
                }
            }
        }
        if (MonitorResourceType.VM_NETWORK_INCOMING.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentVmNetworkInMetrics(metricInfoList)) {
                    return metricInfoList;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_IN);
            defaultMetricInfo.setUnit("bit/s");
            return defaultMetricInfo;
        }
        if (MonitorResourceType.VM_NETWORK_OUTGOING.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentVmNetworkOutMetrics(metricInfoList)) {
                    return metricInfoList;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT);
            defaultMetricInfo.setUnit("bit/s");
            return defaultMetricInfo;
        }
        return null;
    }

    private boolean isAgentCpuMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.CPU_USAGE.equals(metricInfo.getMetricName());
    }

    private boolean isAgentMemMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.MEM_USED_IN_PERCENTAGE.equals(
                metricInfo.getMetricName());
    }

    private boolean isAgentVmNetworkInMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.VM_NET_BIT_SENT.equals(metricInfo.getMetricName());
    }

    private boolean isAgentVmNetworkOutMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.VM_NET_BIT_RECV.equals(metricInfo.getMetricName());
    }

}
