/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformBootRequestFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class of terraform-boot env.
 */
@Configuration
@Profile("terraform-boot")
public class TerraformBootConfig {

    @Getter
    @Value("${terraform.webhook.endpoint}")
    private String clientBaseUri;

    @Getter
    @Value("${webhook.deployCallbackUri}")
    private String deployCallbackUri;

    @Getter
    @Value("${webhook.destroyCallbackUri}")
    private String destroyCallbackUri;

    /**
     * Get the hostname of Xpanse service.
     */
    public String getClientRequestBaseUrl() throws TerraformBootRequestFailedException {
        try {
            if (StringUtils.isBlank(clientBaseUri)) {
                return String.format("http://%s:8080", InetAddress.getLocalHost().getHostAddress());
            } else {
                return clientBaseUri;
            }
        } catch (UnknownHostException e) {
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }
}
