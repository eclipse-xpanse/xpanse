/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on FlexibleEngine cloud.
 */
@Slf4j
@Component
public class FlexibleEngineOrchestratorPlugin implements OrchestratorPlugin {


    @Override
    public DeployResourceHandler getResourceHandler() {
        return new FlexibleTerraformResourceHandler();
    }

    @Override
    public Csp getCsp() {
        return Csp.FLEXIBLE;
    }
}

