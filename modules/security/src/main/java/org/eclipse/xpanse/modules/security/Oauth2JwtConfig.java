/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import org.eclipse.xpanse.modules.security.common.XpanseAuthentication;
import org.eclipse.xpanse.modules.security.common.XpanseAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Beans necessary to manage Oauth2 with OpaqueToken.
 */
@Configuration
@Profile("oauth")
public class Oauth2JwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") // default is set to empty.
    private String issuerUri;

    @Value("${authorization.userid.key}")
    private String userIdKey;

    /**
     * This Converter&lt;Jwt,AbstractAuthenticationToken&gt; must be exposed as a bean to be
     * picked by @WithJwt.
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
            return new XpanseAuthentication(username, authorities, jwt.getClaims(),
                    jwt.getTokenValue());
        };
    }

    @Bean
    @ConditionalOnProperty(name = "authorization.token.type", havingValue = "JWT")
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ofSeconds(60)),
                new JwtIssuerValidator(issuerUri));
        jwtDecoder.setJwtValidator(withClockSkew);
        return jwtDecoder;
    }

}
