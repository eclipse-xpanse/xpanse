/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.config;

import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class TerraBootRestTemplateConfigTest {

    @Mock private RestTemplateLoggingInterceptor mockRestTemplateLoggingInterceptor;

    private TerraBootRestTemplateConfig terraBootRestTemplateConfigUnderTest;

    @BeforeEach
    void setUp() {
        terraBootRestTemplateConfigUnderTest = new TerraBootRestTemplateConfig();
        terraBootRestTemplateConfigUnderTest.restTemplateLoggingInterceptor =
                mockRestTemplateLoggingInterceptor;
    }

    @Test
    void testRestTemplate() {
        // Setup
        // Run the test
        final RestTemplate result = terraBootRestTemplateConfigUnderTest.restTemplate();

        // Verify the results
        Assertions.assertNotNull(result);
    }
}
