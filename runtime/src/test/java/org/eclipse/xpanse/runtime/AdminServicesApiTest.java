/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.cache.consts.CacheConstants;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test for AdminServicesApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties =
                "spring.profiles.active=oauth,zitadel,zitadel-testbed,terraform-boot,tofu-maker,opentelemetry,test,dev")
@AutoConfigureMockMvc
class AdminServicesApiTest extends ApisTestCommon {

    @Value("${spring.datasource.url:jdbc:h2:file:./testdb}")
    private String dataSourceUrl;

    @Resource private PluginManager pluginManager;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testHealthCheck() throws Exception {
        // SetUp
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/health").accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        SystemStatus systemStatus =
                objectMapper.readValue(response.getContentAsString(), SystemStatus.class);
        assertEquals(HealthStatus.OK, systemStatus.getHealthStatus());
        List<BackendSystemStatus> backendSystemStatuses = systemStatus.getBackendSystemStatuses();
        assertEquals(BackendSystemType.values().length, backendSystemStatuses.size());

        assertTrue(
                backendSystemStatuses.stream()
                        .filter(status -> status.getHealthStatus().equals(HealthStatus.NOK))
                        .allMatch(
                                status ->
                                        status.getDetails() != null
                                                && status.getEndpoint() != null));

        assertTrue(
                backendSystemStatuses.stream()
                        .filter(
                                status ->
                                        status.getBackendSystemType()
                                                .equals(BackendSystemType.CACHE_PROVIDER))
                        .allMatch(
                                status ->
                                        status.getEndpoint()
                                                        .equals(
                                                                CacheConstants
                                                                        .CACHE_PROVIDER_CAFFEINE_ENDPOINT)
                                                && status.getName()
                                                        .equals(
                                                                CacheConstants
                                                                        .CACHE_PROVIDER_CAFFEINE)));

        assertTrue(
                backendSystemStatuses.stream()
                        .filter(
                                status ->
                                        status.getBackendSystemType()
                                                .equals(BackendSystemType.DATABASE))
                        .allMatch(
                                status ->
                                        status.getEndpoint().equals(dataSourceUrl)
                                                && status.getName()
                                                        .equals(DatabaseType.H2DB.toValue())));
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testHealthCheckWithRoleNotAdmin() throws Exception {
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/health").accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        SystemStatus systemStatus =
                objectMapper.readValue(response.getContentAsString(), SystemStatus.class);
        assertEquals(HealthStatus.OK, systemStatus.getHealthStatus());
        List<BackendSystemStatus> backendSystemStatuses = systemStatus.getBackendSystemStatuses();
        assertEquals(BackendSystemType.values().length, backendSystemStatuses.size());
        assertTrue(
                backendSystemStatuses.stream()
                        .allMatch(
                                status ->
                                        status.getDetails() == null
                                                && status.getEndpoint() == null));
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetActiveCsps() throws Exception {
        // SetUp
        List<Csp> activeCspList = pluginManager.getPluginsMap().keySet().stream().sorted().toList();

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/csps/active").accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(objectMapper.writeValueAsString(activeCspList), response.getContentAsString());
    }
}
