/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class of terraform-boot env.
 */
@Getter
@Configuration
@Profile("terraform-boot")
public class TerraformBootConfig {

    @Value("${terraform-boot.webhook.endpoint}")
    private String clientBaseUri;

    @Value("${webhook.terraform-boot.deployCallbackUri}")
    private String deployCallbackUri;

    @Value("${webhook.terraform-boot.modifyCallbackUri}")
    private String modifyCallbackUri;

    @Value("${webhook.terraform-boot.destroyCallbackUri}")
    private String destroyCallbackUri;

}
