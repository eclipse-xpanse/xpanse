/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.policy.policyman.config;

import jakarta.annotation.Resource;
import java.util.Collections;
import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/** Configuration class to create a RestTemplate bean for policy-man service API. */
@Configuration
public class PolicyManRestTemplateConfig {

    @Resource RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    /** create a standard RestTemplate bean. */
    @Bean("policyManRestTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(restTemplateLoggingInterceptor));
        return restTemplate;
    }
}
