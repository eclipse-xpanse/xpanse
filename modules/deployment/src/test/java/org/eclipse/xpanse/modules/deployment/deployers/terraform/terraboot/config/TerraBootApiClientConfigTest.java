/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.config;

import static org.mockito.Mockito.verify;

import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.ApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(
        classes = {
            TerraBootApiClientConfig.class,
            DeploymentProperties.class,
            RefreshAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "xpanse.deployer.terra-boot.endpoint=http://localhost:9090",
        })
@ActiveProfiles("terra-boot")
@ExtendWith(SpringExtension.class)
class TerraBootApiClientConfigTest {

    @MockitoBean private ApiClient mockApiClient;

    @Autowired private TerraBootApiClientConfig terraBootApiClientConfigUnderTest;

    @Test
    void testApiClientConfig() {
        // Verify if the setBaePath was called by the SpringFramework
        verify(mockApiClient).setBasePath("http://localhost:9090");
    }
}
