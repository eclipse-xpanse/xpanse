/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configure ApiClient for communication with Terraform-boot service.
 */
@Configuration
public class TerraformBootApiClientConfig {

    @Resource
    private ApiClient apiClient;

    @Value("${terraform-boot.endpoint}")
    private String terraformBootBaseUrl;

    @PostConstruct
    public void apiClientConfig() {
        apiClient.setBasePath(terraformBootBaseUrl);
    }
}
