/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config;

import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.AUTH_TYPE_JWT;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.AUTH_TYPE_TOKEN;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.DEFAULT_ROLE;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.GRANTED_ROLES_KEY;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.USERID_KEY;
import static org.springframework.web.cors.CorsConfiguration.ALL;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.security.zitadel.introspector.ZitadelOpaqueTokenIntrospector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
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
@Profile("zitadel")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class ZitadelWebSecurityConfig {

    @Value("${authorization-token-type:JWT}")
    private String authTokenType;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

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
    public SecurityFilterChain apiFilterChain(HttpSecurity http,
                                              HandlerMappingIntrospector introspector)
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

        if (StringUtils.equalsIgnoreCase(AUTH_TYPE_TOKEN, authTokenType)) {
            // Config custom OpaqueTokenIntrospector
            http.oauth2ResourceServer(oauth2 ->
                    oauth2.opaqueToken(opaque ->
                            opaque.introspector(
                                    new ZitadelOpaqueTokenIntrospector(introspectionUri,
                                            clientId, clientSecret))
                    )
            );
        }

        if (StringUtils.equalsIgnoreCase(AUTH_TYPE_JWT, authTokenType)) {
            // Config custom JwtAuthenticationConverter
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt ->
                            jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                    )
            );
        }
        return http.build();
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

    @Bean
    @ConditionalOnProperty("authorization-token-type=JWT")
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ofSeconds(60)),
                new JwtIssuerValidator(issuerUri));
        jwtDecoder.setJwtValidator(withClockSkew);
        return jwtDecoder;
    }

    Converter<Jwt, ? extends AbstractAuthenticationToken> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new GrantedAuthoritiesExtractor());
        return jwtConverter;
    }

    static class GrantedAuthoritiesExtractor
            implements Converter<Jwt, Collection<GrantedAuthority>> {

        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Set<GrantedAuthority> roles;
            String userId = jwt.getClaimAsString(USERID_KEY);
            Map<String, Object> rolesClaim = jwt.getClaim(GRANTED_ROLES_KEY);
            if (Objects.isNull(rolesClaim) || rolesClaim.isEmpty()) {
                roles = Set.of(new SimpleGrantedAuthority(DEFAULT_ROLE));
                log.info("Get user [id:{}] granted authorities is empty,"
                        + " set default authority user", userId);
            } else {
                roles = rolesClaim.keySet().stream().map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
                log.info("Get user [id:{}] granted authorities:{}.", userId, roles);
            }
            return roles;
        }
    }


}