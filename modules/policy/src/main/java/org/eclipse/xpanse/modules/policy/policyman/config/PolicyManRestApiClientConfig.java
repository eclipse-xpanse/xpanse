/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.policy.policyman.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to update the policy-man endpoint in the client bean.
 */
@Configuration
public class PolicyManRestApiClientConfig {

    @Resource
    private ApiClient apiClient;

    @Value("${policy.man.endpoint}")
    private String policyManBaseUrl;

    @PostConstruct
    public void apiClientConfig() {
        apiClient.setBasePath(policyManBaseUrl);
    }
}
