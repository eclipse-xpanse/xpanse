/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.springframework.stereotype.Component;

@Log
@Component
public class PluginTest implements OrchestratorPlugin {

    private Ocl ocl;

    public Ocl getOcl() {
        return this.ocl;
    }

    @Override
    public DeployResourceHandler getResourceHandler() {
        return null;
    }

    @Override
    public Csp getCsp() {
        return Csp.OPENSTACK;
    }

}
