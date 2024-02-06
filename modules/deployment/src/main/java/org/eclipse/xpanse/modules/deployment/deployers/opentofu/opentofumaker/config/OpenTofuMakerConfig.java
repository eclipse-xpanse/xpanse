/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class of tofu-maker env.
 */
@Getter
@Configuration
@Profile("tofu-maker")
public class OpenTofuMakerConfig {

    @Value("${tofu-maker.webhook.endpoint}")
    private String clientBaseUri;

    @Value("${webhook.tofu-maker.deployCallbackUri}")
    private String deployCallbackUri;

    @Value("${webhook.tofu-maker.destroyCallbackUri}")
    private String destroyCallbackUri;

}
