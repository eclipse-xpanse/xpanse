/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.security.hmac;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/** Security filter for API methods that are protected by HMAC. */
@Configuration
@EnableWebSecurity
public class HmacSecurityFilter {

    private final HmacAuthenticationFilter hmacAuthenticationFilter;

    @Autowired
    public HmacSecurityFilter(HmacAuthenticationFilter hmacAuthenticationFilter) {
        this.hmacAuthenticationFilter = hmacAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterAfter(hmacAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .securityMatcher("/webhook/**")
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Necessary to avoid auto registration of filter as regular HTTP filter chain. This filter must
     * be added only to security filter chain. To avoid adding the filter to regular chain, we must
     * remove the @Component annotation. But it is needed to autowire other dependent beans. see <a
     * href="https://docs.spring.io/spring-security/reference/servlet/architecture.html#_declaring_your_filter_as_a_bean">...</a>
     */
    @Bean
    public FilterRegistrationBean<HmacAuthenticationFilter> hmacFilterRegistration(
            HmacAuthenticationFilter filter) {
        FilterRegistrationBean<HmacAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
