/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth;

import java.util.Collection;
import java.util.Map;
import org.eclipse.xpanse.modules.security.auth.common.XpanseAuthentication;
import org.eclipse.xpanse.modules.security.auth.common.XpanseAuthenticationConverter;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/** Beans necessary to manage Oauth2 with OpaqueToken. */
@Configuration
@Profile("oauth")
public class Oauth2JwtConfig {

    private final OAuth2ResourceServerProperties oauth2ResourceServerProperties;
    private final SecurityProperties securityProperties;
    private final Oauth2JwtDecoder oauth2JwtDecoder;

    /** Constructor method. */
    @Autowired
    public Oauth2JwtConfig(
            OAuth2ResourceServerProperties oauth2ResourceServerProperties,
            SecurityProperties securityProperties,
            Oauth2JwtDecoder oauth2JwtDecoder) {
        this.oauth2ResourceServerProperties = oauth2ResourceServerProperties;
        this.securityProperties = securityProperties;
        this.oauth2JwtDecoder = oauth2JwtDecoder;
    }

    /**
     * This Converter&lt;Jwt,AbstractAuthenticationToken&gt; must be exposed as a bean to be picked
     * by @WithJwt.
     *
     * @param authoritiesConverter convert bean
     * @return XpanseAuthenticationConverter bean
     */
    @Bean
    @ConditionalOnProperty(name = "xpanse.security.oauth.token-type", havingValue = "JWT")
    XpanseAuthenticationConverter jwtAuthenticationConverter(
            Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
        return (Jwt jwt) -> {
            final var username =
                    (String)
                            jwt.getClaims()
                                    .get(securityProperties.getOauth().getClaims().getUserIdKey());
            final var authorities = authoritiesConverter.convert(jwt.getClaims());
            return new XpanseAuthentication(
                    username, authorities, jwt.getClaims(), jwt.getTokenValue());
        };
    }

    @Bean
    @ConditionalOnProperty(name = "xpanse.security.oauth.token-type", havingValue = "JWT")
    JwtDecoder jwtDecoder() {
        return oauth2JwtDecoder.createJwtDecoder(
                oauth2ResourceServerProperties.getJwt().getIssuerUri());
    }
}
