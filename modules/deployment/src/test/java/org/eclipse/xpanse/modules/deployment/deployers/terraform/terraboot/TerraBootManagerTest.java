/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraBootSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class TerraBootManagerTest {

    @Mock private AdminApi mockTerraformApi;

    @Mock private TerraBootHelper terraBootHelper;

    @InjectMocks private TerraBootManager terraBootManagerUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                terraBootManagerUnderTest, "springProfilesActive", "terra-boot");
        ReflectionTestUtils.setField(terraBootManagerUnderTest, "terraBootBaseUrl", "endpoint");
    }

    @Test
    void testGetTerraBootStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TERRA_BOOT);
        expectedResult.setName(BackendSystemType.TERRA_BOOT.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("endpoint");

        // Configure TerraformApi.healthCheck(...).
        final TerraBootSystemStatus terraBootSystemStatus = new TerraBootSystemStatus();
        terraBootSystemStatus.setHealthStatus(TerraBootSystemStatus.HealthStatusEnum.OK);
        when(mockTerraformApi.healthCheck()).thenReturn(terraBootSystemStatus);

        // Run the test
        final BackendSystemStatus result = terraBootManagerUnderTest.getTerraBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetTerraBootStatus_TerraformApiThrowsRestClientException() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TERRA_BOOT);
        expectedResult.setName(BackendSystemType.TERRA_BOOT.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("endpoint");

        when(mockTerraformApi.healthCheck()).thenThrow(RestClientException.class);

        // Run the test
        final BackendSystemStatus result = terraBootManagerUnderTest.getTerraBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetTerraBootStatus_TerraformApiReturnsHealthNotOk() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TERRA_BOOT);
        expectedResult.setName(BackendSystemType.TERRA_BOOT.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("endpoint");

        // Configure TerraformApi.healthCheck(...).
        final TerraBootSystemStatus terraBootSystemStatus = new TerraBootSystemStatus();
        terraBootSystemStatus.setHealthStatus(TerraBootSystemStatus.HealthStatusEnum.NOK);
        when(mockTerraformApi.healthCheck()).thenReturn(terraBootSystemStatus);

        // Run the test
        final BackendSystemStatus result = terraBootManagerUnderTest.getTerraBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetTerraBootStatus_WhenTerraBootNotActive() {
        ReflectionTestUtils.setField(terraBootManagerUnderTest, "springProfilesActive", "oauth");

        // Run the test
        final BackendSystemStatus result = terraBootManagerUnderTest.getTerraBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(null);
    }
}
