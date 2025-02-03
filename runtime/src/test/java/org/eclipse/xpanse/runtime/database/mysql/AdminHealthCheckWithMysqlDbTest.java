/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.controllers.AdminServicesApi;
import org.eclipse.xpanse.modules.cache.consts.CacheConstants;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Test for Health Check with Database mysql and Cache caffeine. */
@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql,test,dev"})
@AutoConfigureMockMvc
class AdminHealthCheckWithMysqlDbTest extends AbstractMysqlIntegrationTest {

    @Value("${spring.datasource.url:jdbc:h2:file:./testdb}")
    private String dataSourceUrl;

    @Resource private AdminServicesApi adminServicesApi;

    @Test
    @WithJwt(file = "jwt_admin.json")
    void testHealthCheck() {
        // SetUp
        // Run the test
        SystemStatus systemStatus = adminServicesApi.healthCheck();
        assertNotNull(systemStatus);
        assertEquals(systemStatus.getHealthStatus(), HealthStatus.OK);
        List<BackendSystemStatus> backendSystemStatuses = systemStatus.getBackendSystemStatuses();
        assertEquals(4, backendSystemStatuses.size());

        assertTrue(backendSystemStatuses.stream().allMatch(status -> status.getEndpoint() != null));

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
                                                        .equals(DatabaseType.MYSQL.toValue())));
    }
}
