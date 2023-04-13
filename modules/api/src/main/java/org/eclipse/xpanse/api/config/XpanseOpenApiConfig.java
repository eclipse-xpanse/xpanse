/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * XpanseOpenApiConfig.
 */
@Configuration
public class XpanseOpenApiConfig implements WebMvcConfigurer {

    public static final String PROJECT_PATH = "file:"
            + System.getProperty("user.dir") + File.separator;

    @Value("${openapi.path:openapi/}")
    private String openapiPath;

    @Value("${openapi.url:/openapi/*}")
    private String openapiUrl;

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    /**
     * registered service openapi.
     *
     * @param registry ResourceHandlerRegistry.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(openapiUrl).addResourceLocations(openapiPath)
                .addResourceLocations(PROJECT_PATH + openapiPath);
    }

    /**
     * CorsOrigin Config.
     *
     * @param registry CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(openapiUrl)
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET")
                .maxAge(3600 * 24);

    }

}
