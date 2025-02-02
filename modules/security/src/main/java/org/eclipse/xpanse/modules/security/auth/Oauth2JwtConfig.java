/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import org.eclipse.xpanse.modules.security.auth.common.XpanseAuthentication;
import org.eclipse.xpanse.modules.security.auth.common.XpanseAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") // default is set to empty.
    private String issuerUri;

    @Value("${authorization.userid.key}")
    private String userIdKey;

    @Resource private Oauth2JwtDecoder oauth2JwtDecoder;

    /**
     * This Converter&lt;Jwt,AbstractAuthenticationToken&gt; must be exposed as a bean to be picked
     * by @WithJwt.
     *
     * @param authoritiesConverter convert bean
     * @return XpanseAuthenticationConverter bean
     */
    @Bean
    @ConditionalOnProperty(name = "authorization.token.type", havingValue = "JWT")
    XpanseAuthenticationConverter jwtAuthenticationConverter(
            Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
        return (Jwt jwt) -> {
            final var username = (String) jwt.getClaims().get(userIdKey);
            final var authorities = authoritiesConverter.convert(jwt.getClaims());
            return new XpanseAuthentication(
                    username, authorities, jwt.getClaims(), jwt.getTokenValue());
        };
    }

    @Bean
    @ConditionalOnProperty(name = "authorization.token.type", havingValue = "JWT")
    JwtDecoder jwtDecoder() {
        return oauth2JwtDecoder.createJwtDecoder(issuerUri);
    }
}
