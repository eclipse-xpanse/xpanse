/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;

/**
 * This interface defines the deployer provider supported by the plugin.
 */
public interface DeployerProvider {

    /**
     * Method to get the full provider script for a given deployer kind and a region.
     *
     * @param deployerKind The kind of deployer.
     * @param region       Name of the region.
     * @return Provider script as string.
     */
    String getProvider(DeployerKind deployerKind, String region);
}
