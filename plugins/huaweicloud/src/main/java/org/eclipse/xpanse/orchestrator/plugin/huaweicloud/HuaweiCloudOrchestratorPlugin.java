/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
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
}

