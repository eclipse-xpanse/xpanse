/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuMakerSystemStatus;
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
class OpenTofuMakerManagerTest {

    @Mock
    private AdminApi mockOpenTofuApi;

    @Mock
    private OpenTofuMakerHelper openTofuMakerHelper;

    @InjectMocks
    private OpenTofuMakerManager openTofuMakerManagerUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openTofuMakerManagerUnderTest, "springProfilesActive",
                "tofu-maker");
        ReflectionTestUtils.setField(openTofuMakerManagerUnderTest, "openTofuMakerBaseUrl",
                "endpoint");
    }

    @Test
    void testGetOpenTofuMakerStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.TOFU_MAKER);
        expectedResult.setName(BackendSystemType.TOFU_MAKER.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("endpoint");

        // Configure OpenTofuApi.healthCheck(...).
        final OpenTofuMakerSystemStatus openTofuMakerSystemStatus = new OpenTofuMakerSystemStatus();
        openTofuMakerSystemStatus.setHealthStatus(OpenTofuMakerSystemStatus.HealthStatusEnum.OK);
        when(mockOpenTofuApi.healthCheck()).thenReturn(openTofuMakerSystemStatus);

        // Run the test
        final BackendSystemStatus result = openTofuMakerManagerUnderTest.getOpenTofuMakerStatus();

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
        expectedResult.setEndpoint("endpoint");

        when(mockOpenTofuApi.healthCheck()).thenThrow(RestClientException.class);

        // Run the test
        final BackendSystemStatus result = openTofuMakerManagerUnderTest.getOpenTofuMakerStatus();

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
        expectedResult.setEndpoint("endpoint");


        // Configure OpenTofuApi.healthCheck(...).
        final OpenTofuMakerSystemStatus openTofuMakerSystemStatus = new OpenTofuMakerSystemStatus();
        openTofuMakerSystemStatus.setHealthStatus(OpenTofuMakerSystemStatus.HealthStatusEnum.NOK);
        when(mockOpenTofuApi.healthCheck()).thenReturn(openTofuMakerSystemStatus);

        // Run the test
        final BackendSystemStatus result = openTofuMakerManagerUnderTest.getOpenTofuMakerStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }


    @Test
    void testGetOpenTofuMakerStatus_WhenOpenTofuMakerNotActive() {
        ReflectionTestUtils.setField(openTofuMakerManagerUnderTest, "springProfilesActive",
                "zitadel");

        // Run the test
        final BackendSystemStatus result = openTofuMakerManagerUnderTest.getOpenTofuMakerStatus();

        // Verify the results
        assertThat(result).isEqualTo(null);
    }
}
