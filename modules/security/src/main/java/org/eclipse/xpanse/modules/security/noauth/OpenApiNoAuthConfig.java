/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.noauth;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Configuration springdoc without security oauth2.
 */
@Profile({"noauth"})
@Configuration
public class OpenApiNoAuthConfig {

    @Value("${app.version}")
    private String appVersion;

    /**
     * Config open api.
     *
     * @return the open api
     */
    @Bean
    public OpenAPI customOpenApi() {

        Info info = new Info().title("XpanseAPI").version(appVersion)
                .description("RESTful Services to interact with Xpanse runtime.");

        return new OpenAPI().info(info);
    }
}