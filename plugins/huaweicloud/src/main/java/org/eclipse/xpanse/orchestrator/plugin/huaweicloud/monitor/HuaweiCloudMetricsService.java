/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor;

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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudMonitorMetrics;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudNameSpaceKind;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudRetryStrategy;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils.HuaweiCloudMonitorCache;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils.HuaweiCloudMonitorClient;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils.HuaweiCloudToXpanseDataModelConverter;
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

    private final HuaweiCloudMonitorCache huaweiCloudMonitorCache;

    private final HuaweiCloudToXpanseDataModelConverter huaweiCloudToXpanseDataModelConverter;

    /**
     * Constructs a HuaweiCloudMetricsService with the necessary dependencies.
     */
    @Autowired
    public HuaweiCloudMetricsService(
            HuaweiCloudMonitorClient huaweiCloudMonitorClient,
            HuaweiCloudMonitorCache huaweiCloudMonitorCache,
            HuaweiCloudToXpanseDataModelConverter huaweiCloudToXpanseDataModelConverter) {
        this.huaweiCloudMonitorClient = huaweiCloudMonitorClient;
        this.huaweiCloudMonitorCache = huaweiCloudMonitorCache;
        this.huaweiCloudToXpanseDataModelConverter = huaweiCloudToXpanseDataModelConverter;
    }

    /**
     * Get metrics of the @deployResource.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsByResource(ResourceMetricRequest resourceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        CredentialDefinition credential = resourceMetricRequest.getCredential();
        MonitorResourceType monitorResourceType = resourceMetricRequest.getMonitorResourceType();
        clearExpiredMetricCache(deployResource.getResourceId());
        ICredential icredential = getIcredential(credential);
        CesClient client = huaweiCloudMonitorClient.getCesClient(icredential,
                deployResource.getProperties().get("region"));
        List<MetricInfoList> targetMetricInfoList =
                getTargetMetricInfoList(deployResource, monitorResourceType,
                        client);
        for (MetricInfoList metricInfoList : targetMetricInfoList) {
            ShowMetricDataRequest showMetricDataRequest =
                    huaweiCloudToXpanseDataModelConverter.buildShowMetricDataRequest(
                            resourceMetricRequest, metricInfoList);
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
                    huaweiCloudToXpanseDataModelConverter.convertShowMetricDataResponseToMetric(
                            deployResource, showMetricDataResponse, metricInfoList);
            cacheResourceMetricsData(metric, metrics, deployResource, metricInfoList);
        }
        return metrics;
    }

    private void cacheResourceMetricsData(Metric metric, List<Metric> metrics,
                                          DeployResource deployResource,
                                          MetricInfoList metricInfoList) {
        if (!CollectionUtils.isEmpty(metric.getMetrics())) {
            metrics.add(metric);
            huaweiCloudMonitorCache.set(deployResource.getResourceId(), metric);
        } else {
            List<Metric> metricCache =
                    huaweiCloudMonitorCache.get(deployResource.getResourceId(),
                            metricInfoList.getMetricName());
            metrics.addAll(metricCache);
        }
    }

    /**
     * Get metrics of the @deployService.
     *
     * @param serviceMetricRequest The request model to query metrics.
     * @return Returns list of metric result.
     */
    public List<Metric> getMetricsByService(ServiceMetricRequest serviceMetricRequest) {
        List<DeployResource> deployResources = serviceMetricRequest.getDeployResources();
        CredentialDefinition credential = serviceMetricRequest.getCredential();
        MonitorResourceType monitorResourceType = serviceMetricRequest.getMonitorResourceType();
        ICredential icredential = getIcredential(credential);
        CesClient client = huaweiCloudMonitorClient.getCesClient(icredential,
                deployResources.get(0).getProperties().get("region"));
        Map<String, List<MetricInfoList>> deployResourceMetricInfoMap = new HashMap<>();
        for (DeployResource deployResource : deployResources) {
            clearExpiredMetricCache(deployResource.getResourceId());
            List<MetricInfoList> targetMetricInfoList =
                    getTargetMetricInfoList(deployResource, monitorResourceType, client);
            deployResourceMetricInfoMap.put(deployResource.getResourceId(), targetMetricInfoList);
        }
        BatchListMetricDataRequest batchListMetricDataRequest =
                huaweiCloudToXpanseDataModelConverter.buildBatchListMetricDataRequest(
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
                huaweiCloudToXpanseDataModelConverter.convertBatchListMetricDataResponseToMetric(
                        batchListMetricDataResponse, deployResourceMetricInfoMap, deployResources);
        cacheServiceMetricsData(metrics, deployResources, serviceMetricRequest);
        return metrics;
    }

    private void cacheServiceMetricsData(List<Metric> metrics, List<DeployResource> deployResources,
                                         ServiceMetricRequest serviceMetricRequest) {
        if (!CollectionUtils.isEmpty(metrics)) {
            for (DeployResource deployResource : deployResources) {
                for (Metric metric : metrics) {
                    String id = metric.getLabels().get("id");
                    if (id.equals(deployResource.getResourceId())) {
                        huaweiCloudMonitorCache.set(deployResource.getResourceId(), metric);
                    }
                }
            }
        } else {
            for (DeployResource deployResource : deployResources) {
                List<Metric> metricList =
                        huaweiCloudMonitorCache.get(deployResource.getResourceId(),
                                serviceMetricRequest.getMonitorResourceType().toValue());
                metrics.addAll(metricList);
            }
        }
    }

    private List<MetricInfoList> getTargetMetricInfoList(DeployResource deployResource,
                                                         MonitorResourceType monitorResourceType,
                                                         CesClient client) {
        List<MetricInfoList> targetMetricInfoLists = new ArrayList<>();
        ListMetricsRequest request =
                huaweiCloudToXpanseDataModelConverter.buildListMetricsRequest(deployResource);
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
        if (Objects.nonNull(listMetricsResponse)) {
            List<MetricInfoList> metricInfoLists = listMetricsResponse.getMetrics();
            if (Objects.isNull(monitorResourceType)) {
                for (MonitorResourceType type : MonitorResourceType.values()) {
                    MetricInfoList targetMetricInfo = getTargetMetricInfo(metricInfoLists, type);
                    if (Objects.nonNull(targetMetricInfo)) {
                        targetMetricInfoLists.add(targetMetricInfo);
                    } else {
                        log.error(
                                "Could not get metrics of the resource. metricType:{},"
                                        + "resourceId:{}",
                                type.toValue(), deployResource.getResourceId());
                    }
                }
            } else {
                MetricInfoList targetMetricInfo =
                        getTargetMetricInfo(metricInfoLists, monitorResourceType);
                if (Objects.nonNull(targetMetricInfo)) {
                    targetMetricInfoLists.add(targetMetricInfo);
                } else {
                    log.error(
                            "Could not get metrics of the resource. metricType:{}, resourceId:{}",
                            monitorResourceType.toValue(), deployResource.getResourceId());
                }
            }
        }
        return targetMetricInfoLists;
    }

    private MetricInfoList getTargetMetricInfo(List<MetricInfoList> metrics,
                                               MonitorResourceType type) {
        if (MonitorResourceType.CPU.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentCpuMetrics(metricInfoList)) {
                    return metricInfoList;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(HuaweiCloudNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(HuaweiCloudMonitorMetrics.CPU_UTILIZED);
            defaultMetricInfo.setUnit("%");
            return defaultMetricInfo;
        }
        if (MonitorResourceType.MEM.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentMemMetrics(metricInfoList)) {
                    return metricInfoList;
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
            defaultMetricInfo.setNamespace(HuaweiCloudNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(HuaweiCloudMonitorMetrics.VM_NETWORK_BANDWIDTH_IN);
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
            defaultMetricInfo.setNamespace(HuaweiCloudNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(HuaweiCloudMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT);
            defaultMetricInfo.setUnit("bit/s");
            return defaultMetricInfo;
        }
        return null;
    }

    private boolean isAgentCpuMetrics(MetricInfoList metricInfo) {
        return HuaweiCloudNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && HuaweiCloudMonitorMetrics.CPU_USAGE.equals(metricInfo.getMetricName());
    }

    private boolean isAgentMemMetrics(MetricInfoList metricInfo) {
        return HuaweiCloudNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && HuaweiCloudMonitorMetrics.MEM_USED_IN_PERCENTAGE.equals(
                metricInfo.getMetricName());
    }

    private boolean isAgentVmNetworkInMetrics(MetricInfoList metricInfo) {
        return HuaweiCloudNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && HuaweiCloudMonitorMetrics.VM_NET_BIT_SENT.equals(metricInfo.getMetricName());
    }

    private boolean isAgentVmNetworkOutMetrics(MetricInfoList metricInfo) {
        return HuaweiCloudNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && HuaweiCloudMonitorMetrics.VM_NET_BIT_RECV.equals(metricInfo.getMetricName());
    }

    /**
     * Clear the expired metric cache.
     */
    private void clearExpiredMetricCache(String resourceId) {
        if (huaweiCloudMonitorCache.getLastClearTime() == 0L) {
            huaweiCloudMonitorCache.setLastClearTime(System.currentTimeMillis());
            return;
        }
        if (System.currentTimeMillis() - huaweiCloudMonitorCache.getLastClearTime()
                > HuaweiCloudMonitorCache.DEFAULT_CACHE_CLEAR_TIME) {
            log.info("start clear expired metric cache.");
            huaweiCloudMonitorCache.expire(resourceId);
            log.info("Successfully cleared expired metric cache.");
            huaweiCloudMonitorCache.setLastClearTime(System.currentTimeMillis());
            log.info("Set the last time to clear expired cache.");
        }
    }

    private ICredential getIcredential(CredentialDefinition credentialDefinition) {
        String accessKey = null;
        String securityKey = null;
        if (CredentialType.VARIABLES.toValue().equals(credentialDefinition.getType().toValue())) {
            List<CredentialVariable> variables = credentialDefinition.getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (HuaweiCloudMonitorConstants.HW_ACCESS_KEY.equals(
                        credentialVariable.getName())) {
                    accessKey = credentialVariable.getValue();
                }
                if (HuaweiCloudMonitorConstants.HW_SECRET_KEY.equals(
                        credentialVariable.getName())) {
                    securityKey = credentialVariable.getValue();
                }
            }
        }
        return huaweiCloudMonitorClient.getIcredentialWithAkSk(accessKey, securityKey);
    }

}