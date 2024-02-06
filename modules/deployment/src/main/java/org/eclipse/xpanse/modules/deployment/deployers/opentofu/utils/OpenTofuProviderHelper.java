/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuProviderNotFoundException;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.springframework.stereotype.Component;

/**
 * Bean to host all helper method related to OpenTofu.
 */
@Component
public class OpenTofuProviderHelper {

    @Resource
    PluginManager pluginManager;

    /**
     * Build the provider OpenTofu file content.
     */
    public String getProvider(Csp csp, String region) {
        String provider = pluginManager.getDeployerProvider(csp, DeployerKind.OPEN_TOFU, region);
        if (StringUtils.isBlank(provider)) {
            String errMsg = String.format("OpenTofu provider for Csp %s not found.", csp);
            throw new OpenTofuProviderNotFoundException(errMsg);
        }
        return provider;
    }

}
