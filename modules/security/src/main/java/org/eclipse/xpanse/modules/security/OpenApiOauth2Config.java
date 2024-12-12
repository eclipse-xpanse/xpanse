/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Configuration springdoc security oauth2. */
@Profile({"oauth"})
@Configuration
public class OpenApiOauth2Config {

    @Value("${app.version}")
    private String appVersion;

    @Value("${oauth.authorization.url}")
    private String authorizationUrl;

    @Value("${oauth.token.url}")
    private String tokenUrl;

    @Value("${authorization.openid.scope}")
    private String openidScope;

    @Value("${authorization.profile.scope}")
    private String profileScope;

    @Value("${authorization.granted.roles.scope}")
    private String rolesScope;

    @Value("${authorization.metadata.scope}")
    private String metadataScope;

    @Value("${enable.role.protection:false}")
    private Boolean roleProtectionIsEnabled;

    /**
     * Config open api.
     *
     * @return the open api
     */
    @Bean
    public OpenAPI customOpenApi() {

        OAuthFlows oauthFlows = new OAuthFlows();
        OAuthFlow oauthFlow =
                new OAuthFlow()
                        .authorizationUrl(authorizationUrl)
                        .tokenUrl(tokenUrl)
                        .scopes(getScopes());
        oauthFlows.authorizationCode(oauthFlow);

        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.flows(oauthFlows).type(SecurityScheme.Type.OAUTH2);
        Components components = new Components();
        String securityName = "OAuth2Flow";
        components.addSecuritySchemes(securityName, securityScheme);
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList(securityName);

        Info info =
                new Info()
                        .title("XpanseAPI")
                        .version(appVersion)
                        .description("RESTful Services to interact with Xpanse runtime.");

        return new OpenAPI().info(info).components(components).addSecurityItem(securityRequirement);
    }

    private Scopes getScopes() {
        Scopes scopes = new Scopes();
        if (roleProtectionIsEnabled) {
            scopes.addString(rolesScope, "mandatory must be selected.");
        }
        scopes.addString(openidScope, "mandatory must be selected.");
        scopes.addString(profileScope, "mandatory must be selected.");
        scopes.addString(metadataScope, "mandatory must be selected.");
        return scopes;
    }
}
