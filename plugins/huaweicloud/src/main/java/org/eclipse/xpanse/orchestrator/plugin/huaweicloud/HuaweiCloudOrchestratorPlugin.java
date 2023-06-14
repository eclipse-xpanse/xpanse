/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.CredentialVariables;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.HuaweiCloudMetricsService;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on Huawei cloud.
 */
@Slf4j
@Component
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin {

    private final DeployResourceHandler resourceHandler = new HuaweiTerraformResourceHandler();

    @Resource
    private HuaweiCloudMetricsService huaweiCloudMetricsService;

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
                        "The access key.", true));
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.", true));
        CredentialVariables accessKey = new CredentialVariables(
                getCsp(), null, HuaweiCloudMonitorConstants.IAM,
                "Using The access key and security key authentication.",
                CredentialType.VARIABLES, credentialVariables);
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(accessKey);
        return credentialInfos;
    }

    @Override
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {
        return huaweiCloudMetricsService.getMetricsByResource(resourceMetricRequest);
    }

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForResource(ResourceMetricRequest resourceMetricRequest) {
        return huaweiCloudMetricsService.getMetricsByResource(resourceMetricRequest);
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricRequest serviceMetricRequest) {
        return huaweiCloudMetricsService.getMetricsByService(serviceMetricRequest);
    }

}
