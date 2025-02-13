/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Configuration class of terra-boot env. */
@Getter
@Configuration
@Profile("terra-boot")
public class TerraBootConfig {

    @Value("${terra-boot.webhook.endpoint}")
    private String clientBaseUri;

    @Value("${webhook.terra-boot.orderCallbackUri}")
    private String orderCallbackUri;
}
