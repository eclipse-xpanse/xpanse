/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.common.config;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Config class. */
@ConfigurationProperties(prefix = "xpanse.openapi-generator")
@Data
public class OpenApiGeneratorProperties {

    @Valid private Client client;

    private String fileGenerationPath;

    private String fileResourcesUri;

    /** Config class. */
    @Data
    public static class Client {
        private String version;
        private String downloadUrl;
    }
}
