/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

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
import org.eclipse.xpanse.orchestrator.plugin.openstack.constants.OpenstackEnvironmentConstants;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.utils.MetricsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * xpanse plugin implementation for openstack cloud.
 */
@Slf4j
@Component
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin {

    private final DeployResourceHandler resourceHandler = new OpenstackTerraformResourceHandler();

    private final MetricsManager metricsManager;

    @Autowired
    public OpenstackOrchestratorPlugin(MetricsManager metricsManager) {
        this.metricsManager = metricsManager;
    }

    /**
     * Get the resource handler for OpenStack.
     */
    @Override
    public DeployResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    /**
     * Get the cloud service provider.
     */
    @Override
    public Csp getCsp() {
        return Csp.OPENSTACK;
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
                new CredentialVariable(OpenstackEnvironmentConstants.AUTH_URL,
                        "The Identity authentication URL."));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.TENANT,
                        "The Name of the Tenant or Project to login with."));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.USERNAME,
                        "The Username to login with."));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.PASSWORD,
                        "The Password to login with."));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.DOMAIN,
                        "The Password to login with."));
        CredentialDefinition httpAuth = new CredentialDefinition(
                getCsp(), "Variables",
                "Authenticate at the specified URL using an account and password.",
                CredentialType.VARIABLES, credentialVariables);
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(httpAuth);
        return credentialInfos;
    }

    @Override
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {
        return this.metricsManager.getMetrics(resourceMetricRequest);
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
