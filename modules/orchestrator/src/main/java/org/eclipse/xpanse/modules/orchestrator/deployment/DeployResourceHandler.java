/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;

/**
 * Handler for the processing the deployment result. The deployer output will
 * be parsed to extract the resources provisioned as part of the deployment
 * process.
 */
public interface DeployResourceHandler {

    /**
     * Handler for the DeployResult.
     *
     * @param deployResult the result of the deployment.
     */
    void handler(DeployResult deployResult);

}
