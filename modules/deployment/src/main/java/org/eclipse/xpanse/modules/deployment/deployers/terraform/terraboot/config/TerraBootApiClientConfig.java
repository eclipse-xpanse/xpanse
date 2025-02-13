/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Configure ApiClient for communication with terra-boot service. */
@Profile("terra-boot")
@Configuration
public class TerraBootApiClientConfig {

    @Resource private ApiClient apiClient;

    @Value("${terra-boot.endpoint}")
    private String terraBootBaseUrl;

    @PostConstruct
    public void apiClientConfig() {
        apiClient.setBasePath(terraBootBaseUrl);
    }
}
