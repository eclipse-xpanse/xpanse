/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth.zitadel.config;

import java.util.Collections;
import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/** Configuration class to create a RestTemplate bean for zitadel. */
@Configuration
@Profile("zitadel")
public class ZitadelRestTemplateConfig {

    private final RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    /** Constructor method. */
    @Autowired
    public ZitadelRestTemplateConfig(
            RestTemplateLoggingInterceptor restTemplateLoggingInterceptor) {
        this.restTemplateLoggingInterceptor = restTemplateLoggingInterceptor;
    }

    /** create a RestTemplate bean named 'zitadelRestTemplate'. */
    @Bean("zitadelRestTemplate")
    public RestTemplate zitadelRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(restTemplateLoggingInterceptor));
        return restTemplate;
    }
}
