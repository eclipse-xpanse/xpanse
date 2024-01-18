/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import java.util.Map;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;

/**
 * This interface describes the DeployResourceHandler used to extract the resources
 * used by the service.
 */
public interface ServiceResourceHandler {


    /**
     * get the resource handler of the CSP plugin.
     *
     * @return the resource handler of the plugin.
     */
    Map<DeployerKind, DeployResourceHandler> resourceHandlers();
}
