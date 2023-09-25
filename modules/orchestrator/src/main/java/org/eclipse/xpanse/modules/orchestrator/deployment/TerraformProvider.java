/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

/**
 * This interface defines the Terraform provider supported by the plugin.
 */
public interface TerraformProvider {

    /**
     * Method to get the full provider script for a given CSP and a region.
     *
     * @param region Name of the region.
     * @return Terraform provider script as string.
     */
    String getProvider(String region);
}
