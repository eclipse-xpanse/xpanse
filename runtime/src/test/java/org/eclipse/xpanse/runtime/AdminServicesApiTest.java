/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.AdminServicesApi;
import org.eclipse.xpanse.modules.models.admin.SystemStatus;
import org.eclipse.xpanse.modules.models.admin.enums.HealthStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for AdminServicesApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {XpanseApplication.class, AdminServicesApi.class})
class AdminServicesApiTest {

    @Autowired
    private AdminServicesApi adminServicesApi;

    @Test
    void testHealthCheck() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        SystemStatus health = adminServicesApi.healthCheck();
        Assertions.assertEquals(systemStatus, health);
    }
}
