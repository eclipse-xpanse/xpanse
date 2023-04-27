/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

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
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on Huawei cloud.
 */
@Slf4j
@Component
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin {

    private final DeployResourceHandler resourceHandler = new HuaweiTerraformResourceHandler();

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
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();

        List<CredentialVariable> credentialVariables = new ArrayList<>();
        CredentialDefinition accessKey = new CredentialDefinition("AKSK",
                "The access key and security key.",
                CredentialType.VARIABLES, credentialVariables);
        credentialVariables.add(new CredentialVariable("HW_ACCESS_KEY", "The access key."));
        credentialVariables.add(new CredentialVariable("HW_SECRET_KEY", "The security key."));

        credentialInfos.add(accessKey);

        return credentialInfos;
    }

    @Override
    public List<Metric> getMetrics(AbstractCredentialInfo credential,
            DeployResource deployResource) {
        return null;
    }
}

