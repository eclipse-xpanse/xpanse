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
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.springframework.stereotype.Component;

/**
 * xpanse plugin implementation for openstack cloud.
 */
@Slf4j
@Component
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin {

    private final DeployResourceHandler resourceHandler = new OpenstackTerraformResourceHandler();

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
                new CredentialVariable("OS_AUTH_URL",
                        "The Identity authentication URL."));
        credentialVariables.add(
                new CredentialVariable("OS_REGION_NAME",
                        "The region of the OpenStack cloud to use."));
        credentialVariables.add(
                new CredentialVariable("OS_TENANT_NAME",
                        "The Name of the Tenant or Project to login with."));
        credentialVariables.add(
                new CredentialVariable("OS_USER_NAME",
                        "The Username to login with."));
        credentialVariables.add(
                new CredentialVariable("OS_PASSWORD",
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
    public List<Metric> getMetrics(AbstractCredentialInfo credential,
                                   DeployResource deployResource,
                                   MonitorResourceType monitorResourceType) {
        return null;
    }
}
