/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.config;

import jakarta.annotation.PostConstruct;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Configure ApiClient for communication with tofu-maker service. */
@Profile("tofu-maker")
@Configuration
public class TofuMakerApiClientConfig {

    private final ApiClient apiClient;

    private final DeploymentProperties deploymentProperties;

    @Autowired
    public TofuMakerApiClientConfig(
            ApiClient apiClient, DeploymentProperties deploymentProperties) {
        this.apiClient = apiClient;
        this.deploymentProperties = deploymentProperties;
    }

    @PostConstruct
    public void apiClientConfig() {
        apiClient.setBasePath(deploymentProperties.getTofuMaker().getEndpoint());
    }
}
