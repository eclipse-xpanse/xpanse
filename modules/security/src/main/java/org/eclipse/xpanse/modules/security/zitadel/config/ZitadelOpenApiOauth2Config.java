/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config;

import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.GRANTED_ROLES_SCOPE;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.METADATA_SCOPE;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.OPENID_SCOPE;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.PROFILE_SCOPE;

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
                scopes = {OPENID_SCOPE, PROFILE_SCOPE, GRANTED_ROLES_SCOPE, METADATA_SCOPE})
)
@SecurityScheme(
        name = "OAuth2 Flow",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode =
        @OAuthFlow(
                authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
                tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
                scopes = {
                        @OAuthScope(name = OPENID_SCOPE,
                                description = "mandatory must be selected."),
                        @OAuthScope(name = PROFILE_SCOPE,
                                description = "mandatory must be selected."),
                        @OAuthScope(name = GRANTED_ROLES_SCOPE,
                                description = "mandatory must be selected.")
                })
        )
)
@Configuration
public class ZitadelOpenApiOauth2Config {
}
