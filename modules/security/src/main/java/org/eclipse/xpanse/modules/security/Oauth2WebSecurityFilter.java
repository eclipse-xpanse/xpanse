/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import static org.springframework.web.cors.CorsConfiguration.ALL;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.security.common.XpanseAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * Configuration applied on all web endpoints defined for this
 * application. Any configuration on specific resources is applied
 * in addition to these global rules.
 */
@Slf4j
@Profile("oauth")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class Oauth2WebSecurityFilter {

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
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            HandlerMappingIntrospector introspector,
            @Nullable Converter<Jwt, XpanseAuthentication> jwtAuthenticationConverter,
            @Nullable OpaqueTokenAuthenticationConverter opaqueTokenAuthenticationConverter)
            throws Exception {
        // accept cors requests and allow preflight checks
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(
                corsConfigurationSource()));

        MvcRequestMatcher.Builder mvcMatcherBuilder =
                new MvcRequestMatcher.Builder(introspector).servletPath("/");

        http.authorizeHttpRequests(arc -> {
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll();
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).permitAll();
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/v3/**")).permitAll();
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/favicon.ico")).permitAll();
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/error")).permitAll();
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/openapi/**")).permitAll();
            arc.requestMatchers(mvcMatcherBuilder.pattern("/xpanse/**")).authenticated();
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/auth/**")).permitAll();
            arc.requestMatchers(AntPathRequestMatcher.antMatcher("/webhook/**")).permitAll();
            arc.anyRequest().authenticated();
        });

        http.csrf(AbstractHttpConfigurer::disable);

        http.headers(headersConfigurer -> headersConfigurer.addHeaderWriter(
                new XFrameOptionsHeaderWriter(
                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)));

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

        if (Objects.nonNull(opaqueTokenAuthenticationConverter)) {
            // Config custom OpaqueTokenIntrospector
            http.oauth2ResourceServer(oauth2 ->
                    oauth2.opaqueToken(opaque ->
                            opaque.introspector(new NimbusOpaqueTokenIntrospector(
                                            introspectionUri, clientId, clientSecret))
                                    .authenticationConverter(opaqueTokenAuthenticationConverter)

                    )
            );
        }

        if (Objects.nonNull(jwtAuthenticationConverter)) {
            // Config custom JwtAuthenticationConverter
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(
                    jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        }
        return http.build();
    }

    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(false); // credentials are not directly accepted.
        configuration.addAllowedHeader(ALL);
        configuration.addAllowedMethod(ALL);
        configuration.addAllowedOriginPattern(ALL);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}