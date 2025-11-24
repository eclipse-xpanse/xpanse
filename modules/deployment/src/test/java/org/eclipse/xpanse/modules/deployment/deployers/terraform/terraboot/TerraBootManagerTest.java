/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraBootSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

@ContextConfiguration(
        classes = {
            TerraBootManager.class,
            DeploymentProperties.class,
            RefreshAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "spring.profiles.active=terra-boot",
            "xpanse.deployer.terra-boot.endpoint=http://localhost:8090"
        })
@ExtendWith(SpringExtension.class)
class TerraBootManagerTest {

    @MockitoBean private AdminApi mockTerraformApi;

    @MockitoBean private TerraBootHelper terraBootHelper;

    @Autowired private TerraBootManager terraBootManagerUnderTest;

    @Test
    void testGetTerraBootStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TERRA_BOOT);
        expectedResult.setName(BackendSystemType.TERRA_BOOT.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("http://localhost:8090");

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
        expectedResult.setEndpoint("http://localhost:8090");

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
        expectedResult.setEndpoint("http://localhost:8090");

        // Configure TerraformApi.healthCheck(...).
        final TerraBootSystemStatus terraBootSystemStatus = new TerraBootSystemStatus();
        terraBootSystemStatus.setHealthStatus(TerraBootSystemStatus.HealthStatusEnum.NOK);
        when(mockTerraformApi.healthCheck()).thenReturn(terraBootSystemStatus);

        // Run the test
        final BackendSystemStatus result = terraBootManagerUnderTest.getTerraBootStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
