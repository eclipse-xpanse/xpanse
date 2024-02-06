/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configure ApiClient for communication with tofu-maker service.
 */
@Profile("tofu-maker")
@Configuration
public class OpenTofuMakerApiClientConfig {

    @Resource
    private ApiClient apiClient;

    @Value("${tofu-maker.endpoint}")
    private String openTofuMakerBaseUrl;

    @PostConstruct
    public void apiClientConfig() {
        apiClient.setBasePath(openTofuMakerBaseUrl);
    }
}
