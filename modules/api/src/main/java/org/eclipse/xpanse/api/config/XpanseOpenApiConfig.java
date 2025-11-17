/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.config;

import java.io.File;
import org.eclipse.xpanse.common.config.OpenApiGeneratorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** XpanseOpenApiConfig. */
@Configuration
public class XpanseOpenApiConfig implements WebMvcConfigurer {

    public static final String PROJECT_PATH =
            "file:" + System.getProperty("user.dir") + File.separator;

    private final OpenApiGeneratorProperties openApiGeneratorProperties;

    @Autowired
    public XpanseOpenApiConfig(OpenApiGeneratorProperties openApiGeneratorProperties) {
        this.openApiGeneratorProperties = openApiGeneratorProperties;
    }

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
        registry.addResourceHandler(openApiGeneratorProperties.getFileResourcesUri())
                .addResourceLocations(openApiGeneratorProperties.getFileGenerationPath())
                .addResourceLocations(
                        PROJECT_PATH + openApiGeneratorProperties.getFileGenerationPath());
    }

    /**
     * CorsOrigin Config.
     *
     * @param registry CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(openApiGeneratorProperties.getFileResourcesUri())
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET")
                .maxAge(3600 * 24);
    }
}
