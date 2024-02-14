/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.DatabaseManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.TofuMakerManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.TerraformBootManager;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.observability.OpenTelemetryCollectorHealthCheck;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.runtime.database.mysql.AbstractMysqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for AdminServicesApi with zitadel and mysql is active.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active="
        + "zitadel,zitadel-testbed,terraform-boot,tofu-maker,mysql"})
@AutoConfigureMockMvc
class AdminServicesApiWithMysqlTest extends AbstractMysqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private MockMvc mockMvc;
    @Resource
    private IdentityProviderManager identityProviderManager;
    @Resource
    private DatabaseManager databaseManager;
    @Resource
    private TerraformBootManager terraformBootManager;
    @Resource
    private PolicyManager policyManager;
    @Resource
    private TofuMakerManager tofuMakerManager;
    @Resource
    private OpenTelemetryCollectorHealthCheck openTelemetryHealthCheck;

    @Test
    void testHealthCheckUnauthorized() throws Exception {
        // SetUp
        Response responseModel = Response.errorResponse(ResultType.UNAUTHORIZED,
                Collections.singletonList(ResultType.UNAUTHORIZED.toValue()));
        String resBody = objectMapper.writeValueAsString(responseModel);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED.value());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());

    }


    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testHealthCheckWithRoleAdmin() throws Exception {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        systemStatus.setBackendSystemStatuses(setUpBackendSystemStatusList(true));
        String resBody = objectMapper.writeValueAsString(systemStatus);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());

    }


    @Test
    @WithJwt(file = "jwt_user.json")
    void testHealthCheckWithRoleNotAdmin() throws Exception {
        // SetUp
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        systemStatus.setBackendSystemStatuses(setUpBackendSystemStatusList(false));
        String resBody = objectMapper.writeValueAsString(systemStatus);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());

    }

    private List<BackendSystemStatus> setUpBackendSystemStatusList(boolean isAdmin) {
        List<BackendSystemStatus> backendSystemStatuses = new ArrayList<>();
        for (BackendSystemType type : BackendSystemType.values()) {
            if (Objects.equals(BackendSystemType.IDENTITY_PROVIDER, type)) {
                BackendSystemStatus identityProviderStatus =
                        identityProviderManager.getActiveIdentityProviderService()
                                .getIdentityProviderStatus();
                if (Objects.nonNull(identityProviderStatus)) {
                    backendSystemStatuses.add(identityProviderStatus);
                }
            }
            if (Objects.equals(BackendSystemType.DATABASE, type)) {
                BackendSystemStatus databaseStatus = databaseManager.getDatabaseStatus();
                if (Objects.nonNull(databaseStatus)) {
                    backendSystemStatuses.add(databaseStatus);
                }
            }
            if (Objects.equals(BackendSystemType.TERRAFORM_BOOT, type)) {
                BackendSystemStatus terraformBootStatus =
                        terraformBootManager.getTerraformBootStatus();
                if (Objects.nonNull(terraformBootStatus)) {
                    backendSystemStatuses.add(terraformBootStatus);
                }
            }
            if (Objects.equals(BackendSystemType.POLICY_MAN, type)) {
                BackendSystemStatus terraformBootStatus =
                        policyManager.getPolicyManStatus();
                if (Objects.nonNull(terraformBootStatus)) {
                    backendSystemStatuses.add(terraformBootStatus);
                }
            }
            if (Objects.nonNull(tofuMakerManager)
                    && type == BackendSystemType.TOFU_MAKER) {
                BackendSystemStatus openTofuMakerStatus =
                        tofuMakerManager.getOpenTofuMakerStatus();
                if (Objects.nonNull(openTofuMakerStatus)) {
                    backendSystemStatuses.add(openTofuMakerStatus);
                }
            }
            if (type == BackendSystemType.OPEN_TELEMETRY_COLLECTOR) {
                BackendSystemStatus otelExporterStatus =
                        openTelemetryHealthCheck.getOpenTelemetryHealthStatus();
                if (Objects.nonNull(otelExporterStatus)) {
                    backendSystemStatuses.add(otelExporterStatus);
                }
            }
        }
        if (!isAdmin) {
            backendSystemStatuses.forEach(backendSystemStatus -> {
                backendSystemStatus.setEndpoint(null);
                backendSystemStatus.setDetails(null);
            });
        }
        return backendSystemStatuses;
    }

}
