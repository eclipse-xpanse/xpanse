/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsRequest;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudNameSpaceKind;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils.HuaweiCloudMonitorCache;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils.HuaweiCloudMonitorClient;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils.HuaweiCloudToXpanseDataModelConverter;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on Huawei cloud.
 */
@Slf4j
@Component
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin {

    private final DeployResourceHandler resourceHandler = new HuaweiTerraformResourceHandler();

    @Resource
    private HuaweiCloudMonitorClient huaweiCloudMonitorClient;

    @Resource
    private HuaweiCloudMonitorCache huaweiCloudMonitorCache;

    /**
     * get HuaweiCloud monitor indicator data and add it to the list.
     */
    private static void processMetricData(ResourceMetricRequest resourceMetricRequest,
                                          HuaweiCloudNameSpaceKind nameSpaceKind,
                                          List<Metric> metrics, CesClient client) {
        List<ShowMetricDataRequest> requestList = HuaweiCloudToXpanseDataModelConverter
                .buildMetricDataRequest(resourceMetricRequest, nameSpaceKind);
        for (ShowMetricDataRequest request : requestList) {
            ShowMetricDataResponse response = client.showMetricData(request);
            Metric metric =
                    HuaweiCloudToXpanseDataModelConverter.convertResponseToMetric(
                            resourceMetricRequest.getDeployResource(),
                            request, response);
            metrics.add(metric);
        }
    }

    @Override
    public DeployResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    @Override
    public Csp getCsp() {
        return Csp.HUAWEI;
    }

    @Override
    public List<CredentialType> getAvailableCredentialTypes() {
        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);
        return credentialTypes;
    }

    @Override
    public List<AbstractCredentialInfo> getCredentialDefinitions() {
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key."));
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key."));
        CredentialDefinition accessKey = new CredentialDefinition(
                getCsp(), HuaweiCloudMonitorConstants.IAM,
                "Using The access key and security key authentication.",
                CredentialType.VARIABLES, credentialVariables);
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(accessKey);
        return credentialInfos;
    }

    @Override
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        DeployResource deployResource = resourceMetricRequest.getDeployResource();
        CredentialDefinition credential = resourceMetricRequest.getCredential();
        MonitorResourceType monitorResourceType = resourceMetricRequest.getMonitorResourceType();
        try {
            clearExpiredMetricCache(deployResource.getResourceId());
            ICredential icredential = getIcredential(credential);
            CesClient client = huaweiCloudMonitorClient.getCesClient(icredential,
                    deployResource.getProperties().get("region"));
            ListMetricsRequest listMetricsRequest =
                    HuaweiCloudToXpanseDataModelConverter.buildListMetricsRequest(deployResource);
            ListMetricsResponse listMetricsResponse = client.listMetrics(listMetricsRequest);
            Set<String> nameSpaceSet =
                    HuaweiCloudToXpanseDataModelConverter.getResponseNamespaces(
                            listMetricsResponse);
            if (nameSpaceSet.contains(HuaweiCloudNameSpaceKind.ECS_AGT.toValue())) {
                processMetricData(resourceMetricRequest,
                        HuaweiCloudNameSpaceKind.ECS_AGT, metrics, client);
            } else {
                processMetricData(resourceMetricRequest,
                        HuaweiCloudNameSpaceKind.ECS_SYS, metrics, client);
            }
            huaweiCloudMonitorCache.set(deployResource.getResourceId(), metrics);
        } catch (ServiceResponseException e) {
            if (huaweiCloudMonitorCache.isEmpty()) {
                return metrics;
            }
            metrics = huaweiCloudMonitorCache.get(deployResource.getResourceId(),
                    monitorResourceType);
        }
        return metrics;
    }

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForResource(ResourceMetricRequest resourceMetricRequest) {
        return null;
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