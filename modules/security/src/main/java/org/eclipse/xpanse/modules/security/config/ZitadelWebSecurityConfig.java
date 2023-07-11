/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration applied on all web endpoints defined for this
 * application. Any configuration on specific resources is applied
 * in addition to these global rules.
 */
@Slf4j
@Profile("zitadel")
@Configuration
@EnableWebSecurity
public class ZitadelWebSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri}")
    private String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
    private String clientSecret;

    /**
     * Configures basic security handler per HTTP session.
     *
     * @param http security configuration
     */
    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(arc -> {
            // add permit for swagger-ui docs and resource
            arc.requestMatchers("/swagger-ui/**", "/v3/**", "/favicon.ico").permitAll();
            // add permit for h2-console html and resource
            arc.requestMatchers("/h2-console/**", "h2/**").permitAll();
            // declarative route configuration
            arc.requestMatchers("/xpanse/**").authenticated();
            // add additional routes
            arc.anyRequest().authenticated();
        });

        http.oauth2ResourceServer(oauth2 -> oauth2
                .opaqueToken(opaque -> opaque
                        .introspectionUri(this.introspectionUri)
                        .introspectionClientCredentials(this.clientId, this.clientSecret)
                )
        );

        return http.build();
    }

}