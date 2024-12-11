/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import java.util.Collection;
import java.util.Map;
import org.eclipse.xpanse.modules.security.common.XpanseAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;

/** Beans necessary to manage Oauth2 with OpaqueToken. */
@Configuration
@Profile("oauth")
public class Oauth2OpaqueTokenConfig {

    @Value("${authorization.userid.key}")
    private String userIdKey;

    /*
     * OpaqueTokenAuthenticationConverter must be exposed as a bean to be
     * picked by @WithOpaqueToken in tests.
     */
    @Bean
    @ConditionalOnProperty(name = "authorization.token.type", havingValue = "OpaqueToken")
    OpaqueTokenAuthenticationConverter opaqueTokenAuthenticationConverter(
            Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
        return (String introspectedToken, OAuth2AuthenticatedPrincipal authenticatedPrincipal) -> {
            final var username = (String) authenticatedPrincipal.getAttributes().get(userIdKey);
            final var authorities =
                    authoritiesConverter.convert(authenticatedPrincipal.getAttributes());
            return new XpanseAuthentication(
                    username,
                    authorities,
                    authenticatedPrincipal.getAttributes(),
                    introspectedToken);
        };
    }
}
