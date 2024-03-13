/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config;

import jakarta.annotation.Resource;
import java.util.Collections;
import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class to create a RestTemplate bean for zitadel.
 */
@Configuration
@Profile("zitadel")
public class ZitadelRestTemplateConfig {

    @Resource
    private RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    /**
     * create a RestTemplate bean named 'zitadelRestTemplate'.
     */
    @Bean("zitadelRestTemplate")
    public RestTemplate zitadelRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(restTemplateLoggingInterceptor));
        return restTemplate;
    }

}
