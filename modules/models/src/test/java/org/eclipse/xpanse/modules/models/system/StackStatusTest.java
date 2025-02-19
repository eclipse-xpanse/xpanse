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
class StackStatusTest {

    private StackStatus stackStatus;

    @BeforeEach
    void setUp() {
        stackStatus = new StackStatus();
        stackStatus.setHealthStatus(HealthStatus.OK);
        BackendSystemStatus backendSystemStatus = new BackendSystemStatus();
        backendSystemStatus.setBackendSystemType(BackendSystemType.DATABASE);
        backendSystemStatus.setHealthStatus(HealthStatus.OK);
        backendSystemStatus.setName(DatabaseType.H2DB.toValue());
        stackStatus.setBackendSystemStatuses(List.of(backendSystemStatus));
    }

    @Test
    void testToString() {
        String expectedToString =
                "StackStatus(healthStatus=OK,"
                        + " backendSystemStatuses=[BackendSystemStatus(backendSystemType=DATABASE,"
                        + " name=h2, healthStatus=OK, endpoint=null, details=null)])";
        Assertions.assertEquals(expectedToString, stackStatus.toString());
    }

    @Test
    public void testGetterAndSetter() {
        Assertions.assertEquals(HealthStatus.OK, stackStatus.getHealthStatus());
        Assertions.assertNotNull(stackStatus.getHealthStatus());
        Assertions.assertEquals(HealthStatus.OK, stackStatus.getHealthStatus());
        Assertions.assertEquals(1, stackStatus.getBackendSystemStatuses().size());
        Assertions.assertEquals(
                BackendSystemType.DATABASE,
                stackStatus.getBackendSystemStatuses().get(0).getBackendSystemType());
        Assertions.assertEquals(
                HealthStatus.OK, stackStatus.getBackendSystemStatuses().get(0).getHealthStatus());
        Assertions.assertEquals(
                DatabaseType.H2DB.toValue(),
                stackStatus.getBackendSystemStatuses().get(0).getName());
    }

    @Test
    public void testEqualsAndHashCode() {
        Assertions.assertEquals(stackStatus, stackStatus);
        Assertions.assertEquals(stackStatus.hashCode(), stackStatus.hashCode());

        Object obj = new Object();
        Assertions.assertNotEquals(stackStatus, obj);
        Assertions.assertNotEquals(stackStatus, null);
        Assertions.assertNotEquals(stackStatus.hashCode(), obj.hashCode());

        StackStatus stackStatus1 = new StackStatus();
        StackStatus stackStatus2 = new StackStatus();
        Assertions.assertNotEquals(stackStatus, stackStatus1);
        Assertions.assertNotEquals(stackStatus, stackStatus2);
        Assertions.assertEquals(stackStatus1, stackStatus2);
        Assertions.assertNotEquals(stackStatus.hashCode(), stackStatus1.hashCode());
        Assertions.assertNotEquals(stackStatus.hashCode(), stackStatus2.hashCode());
        Assertions.assertEquals(stackStatus1.hashCode(), stackStatus2.hashCode());

        stackStatus1.setHealthStatus(HealthStatus.OK);
        stackStatus2.setHealthStatus(HealthStatus.NOK);
        Assertions.assertNotEquals(stackStatus, stackStatus1);
        Assertions.assertNotEquals(stackStatus1, stackStatus2);
        Assertions.assertNotEquals(stackStatus.hashCode(), stackStatus1.hashCode());
        Assertions.assertNotEquals(stackStatus1.hashCode(), stackStatus2.hashCode());

        BackendSystemStatus backendSystemStatus = new BackendSystemStatus();
        backendSystemStatus.setBackendSystemType(BackendSystemType.DATABASE);
        backendSystemStatus.setHealthStatus(HealthStatus.OK);
        backendSystemStatus.setName(DatabaseType.H2DB.toValue());
        stackStatus1.setBackendSystemStatuses(List.of(backendSystemStatus));
        stackStatus2.setBackendSystemStatuses(List.of(new BackendSystemStatus()));
        Assertions.assertEquals(stackStatus, stackStatus1);
        Assertions.assertNotEquals(stackStatus1, stackStatus2);
        Assertions.assertEquals(stackStatus.hashCode(), stackStatus1.hashCode());
        Assertions.assertNotEquals(stackStatus1.hashCode(), stackStatus2.hashCode());
    }
}
