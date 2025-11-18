/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Configuration springdoc security oauth2. */
@Profile({"oauth"})
@Configuration
public class OpenApiOauth2Config {

    private final String appVersion;
    private final SecurityProperties securityProperties;

    @Autowired
    public OpenApiOauth2Config(
            SecurityProperties securityProperties, @Value("${app.version}") String appVersion) {
        this.securityProperties = securityProperties;
        this.appVersion = appVersion;
    }

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
                        .authorizationUrl(securityProperties.getOauth().getAuthUrl())
                        .tokenUrl(securityProperties.getOauth().getTokenUrl())
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
        if (securityProperties.isEnableRoleProtection()) {
            scopes.addString(
                    securityProperties.getOauth().getScopes().getRoles(),
                    "mandatory must be selected.");
        }
        scopes.addString(
                securityProperties.getOauth().getScopes().getOpenid(),
                "mandatory must be selected.");
        scopes.addString(
                securityProperties.getOauth().getScopes().getProfile(),
                "mandatory must be selected.");
        scopes.addString(
                securityProperties.getOauth().getScopes().getMetadata(),
                "mandatory must be selected.");
        return scopes;
    }
}
