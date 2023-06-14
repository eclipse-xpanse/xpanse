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
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.CredentialVariables;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.service.DeployResource;
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
                        "The Identity authentication URL.", false));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.TENANT,
                        "The Name of the Tenant or Project to use.", false));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.USERNAME,
                        "The Username to login with.", false));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.PASSWORD,
                        "The Password to login with.", true));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.DOMAIN,
                        "The domain of the openstack installation to be used.", true));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.PROXY_HOST,
                        "Proxy host with protocol and without port through which to "
                                + "reach the Openstack URL. E.g., http://example.com",
                        false));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.PROXY_PORT,
                        "Proxy port through which to reach the Openstack URL.",
                        false));
        credentialVariables.add(
                new CredentialVariable(OpenstackEnvironmentConstants.SERVICE_TENANT,
                        "Tenant that must be used for accessing services data. "
                                + "If not provided, the OS_TENANT_NAME will be used to access "
                                + "services data.",
                        false));
        CredentialVariables httpAuth = new CredentialVariables(
                getCsp(), null, "Variables",
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
        return this.metricsManager.getMetrics(resourceMetricRequest);
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricRequest serviceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        for (DeployResource deployResource : serviceMetricRequest.getDeployResources()) {
            ResourceMetricRequest resourceMetricRequest = new ResourceMetricRequest(
                    deployResource,
                    serviceMetricRequest.getMonitorResourceType(),
                    serviceMetricRequest.getFrom(),
                    serviceMetricRequest.getTo(),
                    serviceMetricRequest.getGranularity(),
                    serviceMetricRequest.isOnlyLastKnownMetric(),
                    serviceMetricRequest.getXpanseUserName()
            );
            metrics.addAll(this.metricsManager.getMetrics(resourceMetricRequest));
        }
        return metrics;
    }

}
