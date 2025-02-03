/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime.cache.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import java.util.List;
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

/** Test for Health Check with Redis Cache and Database h2. */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev",
            "enable.redis.distributed.cache=true"
        })
@AutoConfigureMockMvc
class AdminHealthCheckWithRedisCacheTest extends AbstractRedisIntegrationTest {

    @Value("${spring.datasource.url:jdbc:h2:file:./testdb}")
    private String dataSourceUrl;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

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
                                        status.getEndpoint().equals(redisHost + ":" + redisPort)
                                                && status.getName()
                                                        .equals(
                                                                CacheConstants
                                                                        .CACHE_PROVIDER_REDIS)));

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
}
