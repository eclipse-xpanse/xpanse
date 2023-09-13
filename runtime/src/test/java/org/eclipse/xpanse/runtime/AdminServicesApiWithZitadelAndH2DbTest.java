/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api.TerraformApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformBootSystemStatus;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.models.system.enums.IdentityProviderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for AdminServicesApi with zitadel and h2db is active.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class AdminServicesApiWithZitadelAndH2DbTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.datasource.url:jdbc:h2:file:./testdb}")
    private String dataSourceUrl;

    @Value("${authorization.server.endpoint}")
    private String iamServerEndpoint;

    @Resource
    private MockMvc mockMvc;

    @Resource
    private TerraformApi terraformApi;

    @Resource
    private ApiClient apiClient;

    private static final String TERRAFORM_BOOT_PROFILE_NAME = "terraform-boot";

    @Value("${spring.profiles.active:}")
    private String springProfilesActive;

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
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());

    }


    @Test
    @WithMockJwtAuth(authorities = {"admin"},
            claims = @OpenIdClaims(sub = "admin-id", preferredUsername = "xpanse-admin"))
    void testHealthCheckWithRoleAdmin() throws Exception {
        // SetUp
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        systemStatus.setBackendSystemStatuses(setUpBackendSystemStatusList(true));
        String resBody = objectMapper.writeValueAsString(systemStatus);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());

    }


    @Test
    @WithMockJwtAuth(authorities = {"user", "csp"},
            claims = @OpenIdClaims(sub = "user-id", preferredUsername = "xpanse-user"))
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
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());

    }

    private List<BackendSystemStatus> setUpBackendSystemStatusList(boolean isAdmin) {
        BackendSystemStatus databaseStatus = new BackendSystemStatus();
        databaseStatus.setBackendSystemType(BackendSystemType.DATABASE);
        databaseStatus.setHealthStatus(HealthStatus.OK);
        databaseStatus.setName(DatabaseType.H2DB.toValue());

        BackendSystemStatus identityProviderStatus = new BackendSystemStatus();
        identityProviderStatus.setBackendSystemType(BackendSystemType.IDENTITY_PROVIDER);
        identityProviderStatus.setHealthStatus(HealthStatus.OK);
        identityProviderStatus.setName(IdentityProviderType.ZITADEL.toValue());

        if (isAdmin) {
            databaseStatus.setEndpoint(dataSourceUrl);
            identityProviderStatus.setEndpoint(iamServerEndpoint);
        }

        List<BackendSystemStatus> backendSystemStatuses = new ArrayList<>();
        backendSystemStatuses.add(identityProviderStatus);
        backendSystemStatuses.add(databaseStatus);

        BackendSystemStatus terraformBootStatus = getTerraformBootStatus();
        if (isAdmin && Objects.nonNull(terraformBootStatus)) {
            terraformBootStatus.setEndpoint(apiClient.getBasePath());
        }
        if (Objects.nonNull(terraformBootStatus)) {
            backendSystemStatuses.add(terraformBootStatus);
        }
        return backendSystemStatuses;
    }

    private BackendSystemStatus getTerraformBootStatus() {
        List<String> configSplitList = Arrays.asList(springProfilesActive.split(","));
        if (configSplitList.contains(TERRAFORM_BOOT_PROFILE_NAME)) {
            BackendSystemStatus terraformBootStatus = new BackendSystemStatus();
            terraformBootStatus.setBackendSystemType(BackendSystemType.TERRAFORM_BOOT);
            terraformBootStatus.setName(BackendSystemType.TERRAFORM_BOOT.toValue());
            if (isTerraformBootApiAccessible()) {
                terraformBootStatus.setHealthStatus(HealthStatus.OK);
            } else {
                terraformBootStatus.setHealthStatus(HealthStatus.NOK);
            }
            return terraformBootStatus;
        }
        return null;
    }

    private boolean isTerraformBootApiAccessible() {
        try {
            TerraformBootSystemStatus terraformBootSystemStatus = terraformApi.healthCheck();
            return terraformBootSystemStatus.getHealthStatus()
                    .equals(TerraformBootSystemStatus.HealthStatusEnum.OK);
        } catch (Exception e) {
            return false;
        }
    }

}
