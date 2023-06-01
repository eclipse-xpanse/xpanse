/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.MetricsService;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on FlexibleEngine cloud.
 */
@Slf4j
@Component
public class FlexibleEngineOrchestratorPlugin implements OrchestratorPlugin {

    private final MetricsService metricsService;

    @Autowired
    public FlexibleEngineOrchestratorPlugin(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public DeployResourceHandler getResourceHandler() {
        return new FlexibleEngineTerraformResourceHandler();
    }

    @Override
    public Csp getCsp() {
        return Csp.FLEXIBLE_ENGINE;
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
        CredentialDefinition accessKey = new CredentialDefinition(
                getCsp(), "AK_SK", "The access key and security key.",
                CredentialType.VARIABLES, credentialVariables);
        credentialVariables.add(
                new CredentialVariable(FlexibleEngineMonitorConstants.OS_ACCESS_KEY,
                        "The access key."));
        credentialVariables.add(
                new CredentialVariable(FlexibleEngineMonitorConstants.OS_SECRET_KEY,
                        "The security key."));
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(accessKey);
        return credentialInfos;
    }

    @Override
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {
        return metricsService.getMetrics(resourceMetricRequest);
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
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricRequest serviceMetricRequest) {
        return null;
    }
}