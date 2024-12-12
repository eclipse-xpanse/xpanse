/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.system;

import java.util.List;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of SystemStatus. */
class SystemStatusTest {

    private SystemStatus systemStatus;

    @BeforeEach
    void setUp() {
        systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        BackendSystemStatus backendSystemStatus = new BackendSystemStatus();
        backendSystemStatus.setBackendSystemType(BackendSystemType.DATABASE);
        backendSystemStatus.setHealthStatus(HealthStatus.OK);
        backendSystemStatus.setName(DatabaseType.H2DB.toValue());
        systemStatus.setBackendSystemStatuses(List.of(backendSystemStatus));
    }

    @Test
    void testToString() {
        String expectedToString =
                "SystemStatus(healthStatus=OK,"
                        + " backendSystemStatuses=[BackendSystemStatus(backendSystemType=DATABASE,"
                        + " name=h2, healthStatus=OK, endpoint=null, details=null)])";
        Assertions.assertEquals(expectedToString, systemStatus.toString());
    }

    @Test
    public void testGetterAndSetter() {
        Assertions.assertEquals(HealthStatus.OK, systemStatus.getHealthStatus());
        Assertions.assertNotNull(systemStatus.getHealthStatus());
        Assertions.assertEquals(HealthStatus.OK, systemStatus.getHealthStatus());
        Assertions.assertEquals(1, systemStatus.getBackendSystemStatuses().size());
        Assertions.assertEquals(
                BackendSystemType.DATABASE,
                systemStatus.getBackendSystemStatuses().get(0).getBackendSystemType());
        Assertions.assertEquals(
                HealthStatus.OK, systemStatus.getBackendSystemStatuses().get(0).getHealthStatus());
        Assertions.assertEquals(
                DatabaseType.H2DB.toValue(),
                systemStatus.getBackendSystemStatuses().get(0).getName());
    }

    @Test
    public void testEqualsAndHashCode() {
        Assertions.assertEquals(systemStatus, systemStatus);
        Assertions.assertEquals(systemStatus.hashCode(), systemStatus.hashCode());

        Object obj = new Object();
        Assertions.assertNotEquals(systemStatus, obj);
        Assertions.assertNotEquals(systemStatus, null);
        Assertions.assertNotEquals(systemStatus.hashCode(), obj.hashCode());

        SystemStatus systemStatus1 = new SystemStatus();
        SystemStatus systemStatus2 = new SystemStatus();
        Assertions.assertNotEquals(systemStatus, systemStatus1);
        Assertions.assertNotEquals(systemStatus, systemStatus2);
        Assertions.assertEquals(systemStatus1, systemStatus2);
        Assertions.assertNotEquals(systemStatus.hashCode(), systemStatus1.hashCode());
        Assertions.assertNotEquals(systemStatus.hashCode(), systemStatus2.hashCode());
        Assertions.assertEquals(systemStatus1.hashCode(), systemStatus2.hashCode());

        systemStatus1.setHealthStatus(HealthStatus.OK);
        systemStatus2.setHealthStatus(HealthStatus.NOK);
        Assertions.assertNotEquals(systemStatus, systemStatus1);
        Assertions.assertNotEquals(systemStatus1, systemStatus2);
        Assertions.assertNotEquals(systemStatus.hashCode(), systemStatus1.hashCode());
        Assertions.assertNotEquals(systemStatus1.hashCode(), systemStatus2.hashCode());

        BackendSystemStatus backendSystemStatus = new BackendSystemStatus();
        backendSystemStatus.setBackendSystemType(BackendSystemType.DATABASE);
        backendSystemStatus.setHealthStatus(HealthStatus.OK);
        backendSystemStatus.setName(DatabaseType.H2DB.toValue());
        systemStatus1.setBackendSystemStatuses(List.of(backendSystemStatus));
        systemStatus2.setBackendSystemStatuses(List.of(new BackendSystemStatus()));
        Assertions.assertEquals(systemStatus, systemStatus1);
        Assertions.assertNotEquals(systemStatus1, systemStatus2);
        Assertions.assertEquals(systemStatus.hashCode(), systemStatus1.hashCode());
        Assertions.assertNotEquals(systemStatus1.hashCode(), systemStatus2.hashCode());
    }
}
