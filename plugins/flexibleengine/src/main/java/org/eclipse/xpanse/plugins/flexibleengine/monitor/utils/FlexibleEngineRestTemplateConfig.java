/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor.utils;

import jakarta.annotation.Resource;
import java.util.Collections;
import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class to create a RestTemplate bean for flexibleEngine.
 */
@Configuration
public class FlexibleEngineRestTemplateConfig {

    @Resource
    private RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    /**
     * create a RestTemplate bean named 'flexibleEngineRestTemplate'.
     */
    @Bean("flexibleEngineRestTemplate")
    public RestTemplate flexibleEngineRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(restTemplateLoggingInterceptor));
        return restTemplate;
    }

}
