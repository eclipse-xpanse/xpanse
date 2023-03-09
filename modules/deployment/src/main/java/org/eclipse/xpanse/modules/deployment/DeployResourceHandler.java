/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import org.eclipse.xpanse.modules.models.service.DeployResult;

/**
 * Handler for the DeployResource.
 */
public interface DeployResourceHandler {

    /**
     * Handler for the DeployResult.
     *
     * @param deployResult the result of the deployment.
     */
    void handler(DeployResult deployResult);

}
