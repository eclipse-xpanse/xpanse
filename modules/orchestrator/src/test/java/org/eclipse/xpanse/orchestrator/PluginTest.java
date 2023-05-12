/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import java.util.List;
import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.springframework.stereotype.Component;

@Log
@Component
public class PluginTest implements OrchestratorPlugin {

    @Override
    public DeployResourceHandler getResourceHandler() {
        return null;
    }

    @Override
    public Csp getCsp() {
        return Csp.OPENSTACK;
    }

    @Override
    public List<CredentialType> getAvailableCredentialTypes() {
        return null;
    }

    @Override
    public List<AbstractCredentialInfo> getCredentialDefinitions() {
        return null;
    }

    @Override
    public List<Metric> getMetrics(AbstractCredentialInfo credential,
            DeployResource deployResource, MonitorResourceType monitorResourceType) {
        return null;
    }
}
