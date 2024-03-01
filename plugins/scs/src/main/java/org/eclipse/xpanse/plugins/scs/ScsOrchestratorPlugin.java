/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.scs.common.constants.ScsEnvironmentConstants;
import org.eclipse.xpanse.plugins.scs.manage.ScsResourceManager;
import org.eclipse.xpanse.plugins.scs.manage.ScsServersManager;
import org.eclipse.xpanse.plugins.scs.resourcehandler.ScsTerraformResourceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * xpanse plugin implementation for SCS cloud.
 */
@Slf4j
@Component
public class ScsOrchestratorPlugin implements OrchestratorPlugin {

    private final ScsTerraformResourceHandler scsTerraformResourceHandler;
    private final ScsServersManager scsServersManager;
    private final ScsResourceManager scsResourceManager;

    /**
     * Constructor.
     */
    @Autowired
    public ScsOrchestratorPlugin(ScsTerraformResourceHandler scsTerraformResourceHandler,
            ScsServersManager scsServersManager,
            ScsResourceManager scsResourceManager) {
        this.scsTerraformResourceHandler = scsTerraformResourceHandler;
        this.scsServersManager = scsServersManager;
        this.scsResourceManager = scsResourceManager;
    }


    /**
     * Get the resource handlers for SCS.
     */
    public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
        Map<DeployerKind, DeployResourceHandler> resourceHandlers = new HashMap<>();
        resourceHandlers.put(DeployerKind.TERRAFORM, scsTerraformResourceHandler);
        resourceHandlers.put(DeployerKind.OPEN_TOFU, scsTerraformResourceHandler);
        return resourceHandlers;
    }

    @Override
    public List<String> getExistingResourcesOfType(String userId, String region,
            DeployResourceKind kind) {
        return scsResourceManager.getExistingResourcesOfType(userId, region, kind);
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
        return Collections.emptyList();
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
                getCsp(), CredentialType.VARIABLES, "USERNAME_PASSWORD",
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

    @Override
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        return scsServersManager.startService(serviceStateManageRequest);
    }

    @Override
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        return scsServersManager.stopService(serviceStateManageRequest);
    }

    @Override
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        return scsServersManager.restartService(serviceStateManageRequest);
    }
}
