/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.scs.constants.ScsEnvironmentConstants;
import org.springframework.stereotype.Component;

/**
 * xpanse plugin implementation for SCS cloud.
 */
@Slf4j
@Component
public class ScsOrchestratorPlugin implements OrchestratorPlugin {

    private final DeployResourceHandler resourceHandler = new ScsTerraformResourceHandler();


    /**
     * Get the resource handler for SCS.
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
        return Csp.SCS;
    }

    @Override
    public List<String> requiredProperties() {
        return List.of(ScsEnvironmentConstants.AUTH_URL);
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
                new CredentialVariable(ScsEnvironmentConstants.PROJECT,
                        "The Name of the Tenant or Project to use.", true, false));
        credentialVariables.add(
                new CredentialVariable(ScsEnvironmentConstants.USERNAME,
                        "The Username to login with.", true, false));
        credentialVariables.add(
                new CredentialVariable(ScsEnvironmentConstants.PASSWORD,
                        "The Password to login with.", true, true));
        credentialVariables.add(
                new CredentialVariable(ScsEnvironmentConstants.DOMAIN,
                        "The domain of the SCS installation to be used.", true, false));
        CredentialVariables httpAuth = new CredentialVariables(
                getCsp(), CredentialType.VARIABLES, "Variables",
                "Authenticate at the specified URL using an account and password.",
                null, credentialVariables);
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(httpAuth);

        /* In the credential definition, object CredentialVariables. The value of fields joined like
           csp-type-name must be unique. It means when you want to add a new CredentialVariables
           with type VARIABLES for this csp, the value of filed name in the new CredentialVariables
           must be different from the value of filed name in others CredentialVariables
           with the same type VARIABLES. Otherwise, it will throw an exception at the application
           startup.
           */
        return credentialInfos;
    }

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric results.
     */
    @Override
    public List<Metric> getMetricsForResource(ResourceMetricsRequest resourceMetricRequest) {
        return Collections.emptyList();
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric results.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        return Collections.emptyList();
    }
}
