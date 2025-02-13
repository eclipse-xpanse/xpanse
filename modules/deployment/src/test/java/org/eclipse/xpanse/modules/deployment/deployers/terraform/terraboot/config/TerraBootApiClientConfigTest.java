/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.config;

import static org.mockito.Mockito.verify;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TerraBootApiClientConfigTest {

    @Mock private ApiClient mockApiClient;

    @InjectMocks private TerraBootApiClientConfig terraBootApiClientConfigUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                terraBootApiClientConfigUnderTest, "terraBootBaseUrl", "http://localhost:9090");
    }

    @Test
    void testApiClientConfig() {
        // Setup
        // Run the test
        terraBootApiClientConfigUnderTest.apiClientConfig();

        // Verify the results
        verify(mockApiClient).setBasePath("http://localhost:9090");
    }
}
