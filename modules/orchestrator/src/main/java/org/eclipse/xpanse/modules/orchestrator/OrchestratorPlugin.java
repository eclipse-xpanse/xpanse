/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator;

import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.credential.AuthenticationCapabilities;
import org.eclipse.xpanse.modules.orchestrator.deployment.ServiceResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.MetricsExporter;

/**
 * This interface describes orchestrator plugin in charge of interacting with backend fundamental
 * APIs.
 */
public interface OrchestratorPlugin
        extends ServiceResourceHandler, AuthenticationCapabilities, MetricsExporter {

    /**
     * get the Csp of the plugin.
     */
    Csp getCsp();
}
