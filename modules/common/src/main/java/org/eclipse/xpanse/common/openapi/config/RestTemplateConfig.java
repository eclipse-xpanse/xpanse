/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.common.openapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class to create a RestTemplate bean.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}