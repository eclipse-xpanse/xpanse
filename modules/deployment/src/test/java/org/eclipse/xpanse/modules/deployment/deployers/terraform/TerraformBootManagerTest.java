/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformBootSystemStatus;
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
class TerraformBootManagerTest {

    @Mock
    private AdminApi mockTerraformApi;

    @InjectMocks
    private TerraformBootManager terraformBootManagerUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(terraformBootManagerUnderTest, "springProfilesActive",
                "terraform-boot");
        ReflectionTestUtils.setField(terraformBootManagerUnderTest, "terraformBootBaseUrl",
                "endpoint");
    }

    @Test
    void testGetTerraformBootStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TERRAFORM_BOOT);
        expectedResult.setName(BackendSystemType.TERRAFORM_BOOT.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("endpoint");

        // Configure TerraformApi.healthCheck(...).
        final TerraformBootSystemStatus terraformBootSystemStatus = new TerraformBootSystemStatus();
        terraformBootSystemStatus.setHealthStatus(TerraformBootSystemStatus.HealthStatusEnum.OK);
        when(mockTerraformApi.healthCheck()).thenReturn(terraformBootSystemStatus);

        // Run the test
        final BackendSystemStatus result = terraformBootManagerUnderTest.getTerraformBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetTerraformBootStatus_TerraformApiThrowsRestClientException() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TERRAFORM_BOOT);
        expectedResult.setName(BackendSystemType.TERRAFORM_BOOT.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("endpoint");

        when(mockTerraformApi.healthCheck()).thenThrow(RestClientException.class);

        // Run the test
        final BackendSystemStatus result = terraformBootManagerUnderTest.getTerraformBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetTerraformBootStatus_TerraformApiReturnsHealthNotOk() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TERRAFORM_BOOT);
        expectedResult.setName(BackendSystemType.TERRAFORM_BOOT.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("endpoint");


        // Configure TerraformApi.healthCheck(...).
        final TerraformBootSystemStatus terraformBootSystemStatus = new TerraformBootSystemStatus();
        terraformBootSystemStatus.setHealthStatus(TerraformBootSystemStatus.HealthStatusEnum.NOK);
        when(mockTerraformApi.healthCheck()).thenReturn(terraformBootSystemStatus);

        // Run the test
        final BackendSystemStatus result = terraformBootManagerUnderTest.getTerraformBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }


    @Test
    void testGetTerraformBootStatus_WhenTerraformBootNotActive() {
        ReflectionTestUtils.setField(terraformBootManagerUnderTest, "springProfilesActive",
                "zitadel");

        // Run the test
        final BackendSystemStatus result = terraformBootManagerUnderTest.getTerraformBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(null);
    }
}
