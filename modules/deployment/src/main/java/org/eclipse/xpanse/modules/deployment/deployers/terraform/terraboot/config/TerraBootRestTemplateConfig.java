/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.config;

import java.util.Collections;
import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/** Configuration class to create a RestTemplate bean for terraBoot. */
@Configuration
public class TerraBootRestTemplateConfig {

    private final RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    /** Constructor method. */
    @Autowired
    public TerraBootRestTemplateConfig(
            RestTemplateLoggingInterceptor restTemplateLoggingInterceptor) {
        this.restTemplateLoggingInterceptor = restTemplateLoggingInterceptor;
    }

    /** create a standard RestTemplate bean. */
    @Primary
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(restTemplateLoggingInterceptor));
        return restTemplate;
    }
}
