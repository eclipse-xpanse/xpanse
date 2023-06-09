/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor;


import com.cloud.apigateway.sdk.utils.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.internal.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.core.internal.model.Project;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.models.FlexibleEngineMonitorMetrics;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.models.FlexibleEngineNameSpaceKind;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils.FlexibleEngineMonitorCache;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils.FlexibleEngineMonitorConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Class to encapsulate all Metric related public methods for FlexibleEngine plugin.
 */
@Slf4j
@Component
public class MetricsService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final FlexibleEngineMonitorConverter flexibleEngineMonitorConverter;

    private final FlexibleEngineMonitorCache flexibleEngineMonitorCache;

    @Autowired
    public MetricsService(
            FlexibleEngineMonitorConverter flexibleEngineMonitorConverter,
            FlexibleEngineMonitorCache flexibleEngineMonitorCache) {
        this.flexibleEngineMonitorConverter = flexibleEngineMonitorConverter;
        this.flexibleEngineMonitorCache = flexibleEngineMonitorCache;
    }

    /**
     * Get HttpRequestBase of the Get request of the FlexibleEngine API.
     *
     * @param credential The credential of the service.
     * @param url        The request url of the FlexibleEngine API.
     * @return Returns HttpRequestBase
     */
    public HttpRequestBase buildGetRequest(CredentialDefinition credential, String url) {
        String accessKey = null;
        String securityKey = null;
        HttpRequestBase requestBase = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = credential.getVariables();
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
        try {
            requestBase = Client.get(accessKey, securityKey, url,
                    flexibleEngineMonitorConverter.getHeaders());
        } catch (Exception e) {
            log.error("Build Get Request of the FlexibleEngine API error.", e);
        }
        return requestBase;
    }

    /**
     * Get HttpRequestBase of the Get request of the FlexibleEngine API.
     *
     * @param credential The credential of the service.
     * @param url        The request url of the FlexibleEngine API.
     * @return Returns HttpRequestBase
     */
    public HttpRequestBase buildPostRequest(CredentialDefinition credential, String url,
            String postbody) {
        String accessKey = null;
        String securityKey = null;
        HttpRequestBase requestBase = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = credential.getVariables();
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
        try {
            requestBase = Client.post(accessKey, securityKey, url,
                    flexibleEngineMonitorConverter.getHeaders(), postbody);
        } catch (Exception e) {
            log.error("Build Post Request of the FlexibleEngine API error.", e);
        }
        return requestBase;
    }

    private Project queryProjectInfo(CredentialDefinition credential, String url) {
        HttpRequestBase requestBase = buildGetRequest(credential, url);
        HttpEntity<String> entity = new HttpEntity<>("parameters", getHttpHeaders(requestBase));
        try {
            ResponseEntity<KeystoneListProjectsResponse> response =
                    restTemplate.exchange(requestBase.getURI(), HttpMethod.GET, entity,
                            KeystoneListProjectsResponse.class);
            if (Objects.nonNull(response.getBody())) {
                if (!CollectionUtils.isEmpty(response.getBody().getProjects())) {
                    return response.getBody().getProjects().get(0);
                }
            }
        } catch (RestClientException e) {
            log.error("query metrics info by url:{} error.", url, e);
        }
        return null;
    }

    private ShowMetricDataResponse queryMetricsInfo(CredentialDefinition credential, String url) {
        HttpRequestBase requestBase = buildGetRequest(credential, url);
        HttpEntity<String> entity = new HttpEntity<>("parameters", getHttpHeaders(requestBase));
        ShowMetricDataResponse result = null;
        try {
            ResponseEntity<ShowMetricDataResponse> responseEntity =
                    restTemplate.exchange(url, HttpMethod.GET, entity,
                            ShowMetricDataResponse.class);
            result = responseEntity.getBody();
        } catch (RestClientException e) {
            log.error("query metrics info by url:{} error.", url, e);
        }
        return result;
    }

    private ListMetricsResponse queryListMetricsInfo(CredentialDefinition credential, String url) {
        HttpRequestBase requestBase = buildGetRequest(credential, url);
        HttpEntity<String> entity = new HttpEntity<>("parameters", getHttpHeaders(requestBase));
        ListMetricsResponse result = null;
        try {
            ResponseEntity<ListMetricsResponse> responseEntity =
                    restTemplate.exchange(url, HttpMethod.GET, entity, ListMetricsResponse.class);
            result = responseEntity.getBody();
        } catch (RestClientException e) {
            log.error("query metrics list info by url:{} error.", url, e);
        }
        return result;
    }

    private BatchListMetricDataResponse batchQueryListMetricsInfo(CredentialDefinition credential,
            BatchListMetricDataRequest request, String url) {
        BatchListMetricDataResponse result = null;
        try {
            String requestBody = new ObjectMapper().writeValueAsString(request.getBody());
            HttpRequestBase requestBase = buildPostRequest(credential, url, requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBody,
                    getHttpHeaders(requestBase));
            ResponseEntity<BatchListMetricDataResponse> responseEntity =
                    restTemplate.exchange(url, HttpMethod.POST, entity,
                            BatchListMetricDataResponse.class);
            result = responseEntity.getBody();
        } catch (RestClientException | JsonProcessingException e) {
            log.error("batch query metrics list info by url:{} error.", url, e);
        }
        return result;
    }

    private HttpHeaders getHttpHeaders(HttpRequestBase httpRequestBase) {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> headerKeyValues = Arrays.stream(httpRequestBase.getAllHeaders())
                .collect(Collectors.toMap(Header::getName, Header::getValue));
        headers.setAll(headerKeyValues);
        return headers;
    }

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {
        return getMetricsForResource(resourceMetricRequest);
    }

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsForResource(ResourceMetricRequest resourceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        CredentialDefinition credential = resourceMetricRequest.getCredential();
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        String region = deployResource.getProperties().get("region");
        MonitorResourceType resourceType = resourceMetricRequest.getMonitorResourceType();
        if (StringUtils.isNotBlank(region)) {
            String projectQueryUrl =
                    flexibleEngineMonitorConverter.buildProjectQueryUrl(region).toString();
            Project project =
                    queryProjectInfo(resourceMetricRequest.getCredential(), projectQueryUrl);
            if (Objects.nonNull(project) && StringUtils.isNotBlank(project.getId())) {
                List<MetricInfoList> targetMetrics =
                        getTargetMetricInfoList(deployResource, credential, resourceType, project);
                for (MetricInfoList metricInfoList : targetMetrics) {
                    String url = flexibleEngineMonitorConverter.buildMonitorMetricUrl(
                            resourceMetricRequest, project.getId(), metricInfoList);
                    if (StringUtils.isNotBlank(url)) {
                        ShowMetricDataResponse response = queryMetricsInfo(credential, url);
                        Metric metric =
                                flexibleEngineMonitorConverter.convertResponseToMetric(
                                        deployResource, response, metricInfoList);

                        if (!CollectionUtils.isEmpty(metric.getMetrics())) {
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
        CredentialDefinition credential = serviceMetricRequest.getCredential();
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
            return new ArrayList<>();
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
                batchQueryListMetricsInfo(credential, batchListMetricDataRequest,
                        url);

        List<Metric> metrics =
                FlexibleEngineMonitorConverter.convertBatchListMetricDataResponseToMetric(
                        batchListMetricDataResponse, deployResourceMetricInfoMap, deployResources);
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
                                                         CredentialDefinition credential,
                                                         MonitorResourceType monitorResourceType,
                                                         Project project) {

        List<MetricInfoList> targetMetrics = new ArrayList<>();
        String url =
                flexibleEngineMonitorConverter.buildListMetricsUrl(deployResource,
                        project.getId());
        ListMetricsResponse listMetricsResponse = queryListMetricsInfo(credential, url);
        if (Objects.nonNull(listMetricsResponse)) {
            List<MetricInfoList> metrics = listMetricsResponse.getMetrics();
            if (Objects.isNull(monitorResourceType)) {
                for (MonitorResourceType type : MonitorResourceType.values()) {
                    MetricInfoList targetMetricInfo = getTargetMetricInfo(metrics, type);
                    if (Objects.nonNull(targetMetricInfo)) {
                        targetMetrics.add(targetMetricInfo);
                    } else {
                        log.error("Could not get metrics of the resource. metricType:{},"
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
}
