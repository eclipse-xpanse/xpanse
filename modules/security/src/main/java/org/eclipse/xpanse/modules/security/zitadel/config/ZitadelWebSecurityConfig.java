/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config;

import static org.springframework.web.cors.CorsConfiguration.ALL;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.security.zitadel.introspector.ZitadelAuthoritiesOpaqueTokenIntrospector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration applied on all web endpoints defined for this
 * application. Any configuration on specific resources is applied
 * in addition to these global rules.
 */
@Slf4j
@Profile("zitadel")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
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
        // accept cors requests and allow preflight checks
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(
                corsConfigurationSource()));
        http.authorizeHttpRequests(arc -> {
            // add permit for swagger-ui docs and resource
            arc.requestMatchers("/swagger-ui/**", "/v3/**", "/favicon.ico", "/error").permitAll();
            // add permit for h2-console html and resource
            arc.requestMatchers("/h2-console/**", "h2/**").permitAll();
            // add permit for auth apis
            arc.requestMatchers("/auth/**").permitAll();
            // declarative route configuration
            arc.requestMatchers("/xpanse/**").authenticated();
            // add additional routes
            arc.anyRequest().authenticated();
        });

        // use custom implementation of OpaqueTokenIntrospector
        http.oauth2ResourceServer(oauth2 ->
                oauth2.opaqueToken(opaque ->
                        opaque.introspector(zitadelIntrospector())
                )
        );

        // set custom exception handler
        http.exceptionHandling(exceptionHandlingConfigurer ->
                exceptionHandlingConfigurer.authenticationEntryPoint(
                        (httpRequest, httpResponse, authException) -> {
                            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            httpResponse.setCharacterEncoding("UTF-8");
                            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ObjectMapper objectMapper = new ObjectMapper();
                            Response responseModel = Response.errorResponse(ResultType.UNAUTHORIZED,
                                    Collections.singletonList(ResultType.UNAUTHORIZED.toValue()));
                            String resBody = objectMapper.writeValueAsString(responseModel);
                            PrintWriter printWriter = httpResponse.getWriter();
                            printWriter.print(resBody);
                            printWriter.flush();
                            printWriter.close();
                        }
                ));

        return http.build();
    }

    /**
     * Customize the ZitadelAuthoritiesOpaqueTokenIntrospector as the implementation class of
     * OpaqueTokenIntrospector to instead of the SpringOpaqueTokenIntrospector
     * as default implementation class.
     * The SpringOpaqueTokenIntrospector get roles of user from the filed 'authorities' default.
     * The IAM 'zitadel' put roles of user into other filed not 'authorities'. In this case,
     * we could not get roles of user the default SpringOpaqueTokenIntrospector.
     * So we need to customize the ZitadelAuthoritiesOpaqueTokenIntrospector, in which
     * we get the roles of user from the real filed.
     *
     * @return custom NimbusOpaqueTokenIntrospector
     */
    @Bean
    public OpaqueTokenIntrospector zitadelIntrospector() {
        return new ZitadelAuthoritiesOpaqueTokenIntrospector(
                this.introspectionUri,
                this.clientId,
                this.clientSecret);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader(ALL);
        configuration.addAllowedMethod(ALL);
        configuration.addAllowedOriginPattern(ALL);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}