/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.DatabaseManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.TerraformBootManager;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.policy.policyman.PolicyManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.CollectionUtils;

/**
 * Test for AdminServicesApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class AdminServicesApiTest extends AbstractJwtTestConfiguration {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MockMvc mockMvc;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private IdentityProviderManager identityProviderManager;
    @Resource
    private DatabaseManager databaseManager;
    @Resource
    private TerraformBootManager terraformBootManager;
    @Resource
    private PolicyManager policyManager;

    @Test
    @WithMockAuthentication(authType = JwtAuthenticationToken.class)
    void testHealthCheck() throws Exception {
        // SetUp
        super.updateJwtInSecurityContext(Collections.emptyMap(), Collections.singletonList(ROLE_ADMIN));
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        List<BackendSystemStatus> backendSystemStatuses = setUpBackendSystemStatusList(true);
        if (!CollectionUtils.isEmpty(backendSystemStatuses)) {
            systemStatus.setBackendSystemStatuses(backendSystemStatuses);
        }

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
    @WithMockAuthentication(authType = JwtAuthenticationToken.class)
    void testHealthCheckWithRoleNotAdmin() throws Exception {
        // SetUp
        super.updateJwtInSecurityContext(Collections.emptyMap(), Collections.singletonList("user"));
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        List<BackendSystemStatus> backendSystemStatuses = setUpBackendSystemStatusList(false);
        if (!CollectionUtils.isEmpty(backendSystemStatuses)) {
            systemStatus.setBackendSystemStatuses(backendSystemStatuses);
        }

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
    @WithMockAuthentication(authType = JwtAuthenticationToken.class)
    void testGetCsps() throws Exception {
        // SetUp
        super.updateJwtInSecurityContext(Collections.emptyMap(), Collections.singletonList(ROLE_ADMIN));
        List<Csp> cspList = Arrays.asList(Csp.values());
        String resultBody = objectMapper.writeValueAsString(cspList);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/csp")
                        .param("active", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resultBody, response.getContentAsString());
    }

    @Test
    @WithMockAuthentication(authType = JwtAuthenticationToken.class)
    void testGetCspsWithActive() throws Exception {
        // SetUp
        super.updateJwtInSecurityContext(Collections.emptyMap(), Collections.singletonList(ROLE_ADMIN));
        List<Csp> cspList = pluginManager.getPluginsMap().keySet().stream().sorted().toList();
        String resultBody = objectMapper.writeValueAsString(cspList);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/csp")
                        .param("active", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resultBody, response.getContentAsString());
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
