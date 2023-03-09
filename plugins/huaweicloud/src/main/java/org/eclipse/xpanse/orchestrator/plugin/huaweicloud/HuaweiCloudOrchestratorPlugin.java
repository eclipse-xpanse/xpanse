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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on Huawei cloud.
 */
@Slf4j
@Component
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin {

    private static final int STATUS_MSG_MAX_LENGTH = 255;

    private final Environment environment;

    private final ApplicationContext context;

    private final DeployResourceHandler resourceHandler = new HuaweiTerraformResourceHandler();

    /**
     * Default constructor for the HuaweiCloudOrchestratorPlugin bean.
     *
     * @param environment Environment bean from Spring framework
     * @param context     Application context of the Spring framework
     */
    @Autowired
    public HuaweiCloudOrchestratorPlugin(Environment environment,
            ApplicationContext context) {
        this.environment = environment;
        this.context = context;
    }

    @Override
    public DeployResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    @Override
    public Csp getCsp() {
        return Csp.HUAWEI;
    }
}

