/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Configuration springdoc security oauth2.
 */
@Profile("zitadel")
@OpenAPIDefinition(
        info = @Info(
                title = "Xpanse API",
                description = "RESTful Services to interact with Xpanse runtime",
                version = "${app.version}"
        ),
        security = @SecurityRequirement(name = "OAuth2 Flow",
                scopes = {"openid", "email", "profile"})
)
@SecurityScheme(
        name = "OAuth2 Flow",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode =
        @OAuthFlow(
                authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
                tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
                scopes = {
                        @OAuthScope(name = "openid", description = "mandatory must be selected"),
                        @OAuthScope(name = "profile", description = "mandatory must be selected"),
                        @OAuthScope(name = "email")
                }
        )
        )
)
@Configuration
public class ZitadelOpenApiOauth2Config {
}
