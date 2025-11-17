/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.policy.policyman.config;

import jakarta.annotation.PostConstruct;
import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;
import org.springframework.context.annotation.Configuration;

/** Configuration class to update the policy-man endpoint in the client bean. */
@Configuration
public class PolicyManRestApiClientConfig {

    private final ApiClient apiClient;

    private final PolicyManProperties policyManProperties;

    public PolicyManRestApiClientConfig(
            ApiClient apiClient, PolicyManProperties policyManProperties) {
        this.apiClient = apiClient;
        this.policyManProperties = policyManProperties;
    }

    @PostConstruct
    public void apiClientConfig() {
        apiClient.setBasePath(policyManProperties.getEndpoint());
    }
}
