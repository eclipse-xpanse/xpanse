/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

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
@Profile("oauth")
@OpenAPIDefinition(
        info = @Info(
                title = "Xpanse API",
                description = "RESTful Services to interact with Xpanse runtime",
                version = "${app.version}"
        ),
        security = @SecurityRequirement(name = "OAuth2Flow",
                scopes = {"${authorization.openid.scope}", "${authorization.profile.scope}",
                        "${authorization.granted.roles.scope}", "${authorization.metadata.scope}"})
)
@SecurityScheme(
        name = "OAuth2Flow",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode =
        @OAuthFlow(
                authorizationUrl = "${oauth.authorization.url}",
                tokenUrl = "${oauth.token.url}",
                scopes = {
                        @OAuthScope(name = "${authorization.openid.scope}",
                                description = "mandatory must be selected."),
                        @OAuthScope(name = "${authorization.profile.scope}",
                                description = "mandatory must be selected."),
                        @OAuthScope(name = "${authorization.granted.roles.scope}",
                                description = "mandatory must be selected."),
                        @OAuthScope(name = "${authorization.metadata.scope}",
                                description = "mandatory must be selected.")
                })
        )
)
@Configuration
public class OpenApiOauth2Config {
}
