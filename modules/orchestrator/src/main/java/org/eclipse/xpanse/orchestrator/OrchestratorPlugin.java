/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import org.eclipse.xpanse.modules.credential.AuthenticationCapabilities;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.monitor.MetricsExporter;
import org.eclipse.xpanse.orchestrator.service.ServiceResourceHandler;

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
