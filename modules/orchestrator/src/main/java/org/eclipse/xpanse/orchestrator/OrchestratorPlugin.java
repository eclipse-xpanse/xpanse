/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * This interface describes orchestrator plugin in charge of interacting with backend fundamental
 * APIs.
 */
public interface OrchestratorPlugin {

    /**
     * get the resource handler of the CSP plugin.
     *
     * @return the resource handler of the plugin.
     */
    DeployResourceHandler getResourceHandler();


    /**
     * get the Csp of the plugin.
     */
    Csp getCsp();
}
