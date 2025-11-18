/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.policy.policyman.config;

import jakarta.annotation.PostConstruct;
import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/** Configuration class to update the policy-man endpoint in the client bean. */
@Configuration
public class PolicyManRestApiClientConfig {

    private final ApiClient apiClient;

    private final PolicyManProperties policyManProperties;

    /** Constructor method. */
    public PolicyManRestApiClientConfig(
            ApiClient apiClient, PolicyManProperties policyManProperties) {
        this.apiClient = apiClient;
        this.policyManProperties = policyManProperties;
    }

    @PostConstruct
    @EventListener(RefreshScopeRefreshedEvent.class)
    public void apiClientConfig() {
        apiClient.setBasePath(policyManProperties.getEndpoint());
    }
}
