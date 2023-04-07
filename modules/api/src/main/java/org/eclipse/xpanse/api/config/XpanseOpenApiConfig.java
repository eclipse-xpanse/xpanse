/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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

}
