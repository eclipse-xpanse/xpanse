/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.utils;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformProviderNotFoundException;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.springframework.stereotype.Component;

/**
 * Bean to host all helper method related to Terraform.
 */
@Component
public class TerraformProviderHelper {

    @Resource
    PluginManager pluginManager;

    /**
     * Build the provider terraform file content.
     */
    public String getProvider(Csp csp, String region) {
        String provider = pluginManager.getDeployerProvider(csp, DeployerKind.TERRAFORM, region);
        if (StringUtils.isBlank(provider)) {
            String errMsg = String.format("Terraform provider for Csp %s not found.", csp);
            throw new TerraformProviderNotFoundException(errMsg);
        }
        return provider;
    }

}
