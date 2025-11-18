/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.ai.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/** Config class. */
@RefreshScope
@ConfigurationProperties(prefix = "xpanse.ai")
@Validated
@Data
public class AiProperties {

    @Valid private Llm llm;

    @Valid private Docker docker;

    /** Config class. */
    @Data
    public static class Llm {
        @NotBlank private String providerApiEndpointUrl;

        @NotBlank private String modelName;

        private String providerApiKey = "";
    }

    /** Config class. */
    @Data
    public static class Docker {
        @NotBlank private String registryUrl;

        private String registryUsername = "";

        private String registryPassword = "";

        private String registryOrganization = "";

        private String proxyUrl = "";
    }
}
