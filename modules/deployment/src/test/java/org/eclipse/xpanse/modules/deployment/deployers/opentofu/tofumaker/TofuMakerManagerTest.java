/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.TofuMakerSystemStatus;
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
            DeploymentProperties.class,
            TofuMakerManager.class,
            RefreshAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "spring.profiles.active=tofu-maker",
            "xpanse.deployer.tofu-maker.endpoint=http://localhost:8090"
        })
@ExtendWith(SpringExtension.class)
class TofuMakerManagerTest {

    @MockitoBean private AdminApi mockOpenTofuApi;

    @Autowired private TofuMakerManager tofuMakerManagerUnderTest;

    @Test
    void testGetOpenTofuMakerStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TOFU_MAKER);
        expectedResult.setName(BackendSystemType.TOFU_MAKER.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("http://localhost:8090");

        // Configure OpenTofuApi.healthCheck(...).
        final TofuMakerSystemStatus tofuMakerSystemStatus = new TofuMakerSystemStatus();
        tofuMakerSystemStatus.setHealthStatus(TofuMakerSystemStatus.HealthStatusEnum.OK);
        when(mockOpenTofuApi.healthCheck()).thenReturn(tofuMakerSystemStatus);

        // Run the test
        final BackendSystemStatus result = tofuMakerManagerUnderTest.getOpenTofuMakerStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetOpenTofuMakerStatus_OpenTofuApiThrowsRestClientException() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TOFU_MAKER);
        expectedResult.setName(BackendSystemType.TOFU_MAKER.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("http://localhost:8090");

        when(mockOpenTofuApi.healthCheck()).thenThrow(RestClientException.class);

        // Run the test
        final BackendSystemStatus result = tofuMakerManagerUnderTest.getOpenTofuMakerStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetOpenTofuMakerStatus_OpenTofuApiReturnsHealthNotOk() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TOFU_MAKER);
        expectedResult.setName(BackendSystemType.TOFU_MAKER.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("http://localhost:8090");

        // Configure OpenTofuApi.healthCheck(...).
        final TofuMakerSystemStatus tofuMakerSystemStatus = new TofuMakerSystemStatus();
        tofuMakerSystemStatus.setHealthStatus(TofuMakerSystemStatus.HealthStatusEnum.NOK);
        when(mockOpenTofuApi.healthCheck()).thenReturn(tofuMakerSystemStatus);

        // Run the test
        final BackendSystemStatus result = tofuMakerManagerUnderTest.getOpenTofuMakerStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
