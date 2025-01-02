/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.orchestrator.audit.OperationalAudit;
import org.eclipse.xpanse.modules.orchestrator.credential.AuthenticationCapabilities;
import org.eclipse.xpanse.modules.orchestrator.deployment.ServiceResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsExporter;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceCalculator;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManager;
import org.eclipse.xpanse.modules.orchestrator.servicetemplate.ServiceTemplateManager;

/**
 * This interface describes orchestrator plugin in charge of interacting with backend fundamental
 * APIs.
 */
public interface OrchestratorPlugin
        extends ServiceResourceHandler,
                AuthenticationCapabilities,
                ServiceMetricsExporter,
                ServiceStateManager,
                OperationalAudit,
                ServicePriceCalculator,
                ServiceTemplateManager {

    /**
     * Get the Csp of the plugin.
     *
     * @return cloud service provider.
     */
    Csp getCsp();

    /**
     * Get the required properties of the plugin.
     *
     * @return required properties.
     */
    List<String> requiredProperties();

    /** Get all sites of the cloud service provider. */
    List<String> getSites();

    /**
     * Validate regions of service.
     *
     * @param ocl Ocl object.
     * @return true if all regions are valid.
     */
    boolean validateRegionsOfService(Ocl ocl);

    Map<String, String> getComputeResourcesInServiceDeployment(File scriptFile);
}
