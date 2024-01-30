/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configure ApiClient for communication with Terraform-boot service.
 */
@Profile("terraform-boot")
@Configuration
public class TerraformBootApiClientConfig {

    @Resource
    private ApiClient apiClient;

    @Value("${terraform.boot.endpoint}")
    private String terraformBootBaseUrl;

    @PostConstruct
    public void apiClientConfig() {
        apiClient.setBasePath(terraformBootBaseUrl);
    }
}
