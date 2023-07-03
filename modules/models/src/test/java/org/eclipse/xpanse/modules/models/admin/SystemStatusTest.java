/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.eclipse.xpanse.modules.models.admin.enums.HealthStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of SystemStatus.
 */
class SystemStatusTest {

    private static SystemStatus systemStatus;
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @BeforeEach
    void setUp() {
        systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
    }

    @Test
    public void testHealthStatusNotNull() {
        Assertions.assertNotNull(systemStatus.getHealthStatus());
        Assertions.assertEquals(0, validator.validate(systemStatus).size());
    }

    @Test
    void testToString() {
        String expectedToString = "SystemStatus(healthStatus=OK)";
        assertEquals(expectedToString, systemStatus.toString());
    }

    @Test
    public void testGetterAndSetter() {
        assertEquals(HealthStatus.OK, systemStatus.getHealthStatus());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(systemStatus, systemStatus);
        assertEquals(systemStatus.hashCode(), systemStatus.hashCode());

        Object obj = new Object();
        assertNotEquals(systemStatus, obj);
        assertNotEquals(systemStatus, null);
        assertNotEquals(systemStatus.hashCode(), obj.hashCode());

        SystemStatus systemStatus1 = new SystemStatus();
        SystemStatus systemStatus2 = new SystemStatus();
        assertNotEquals(systemStatus, systemStatus1);
        assertNotEquals(systemStatus, systemStatus2);
        assertEquals(systemStatus1, systemStatus2);
        assertNotEquals(systemStatus.hashCode(), systemStatus1.hashCode());
        assertNotEquals(systemStatus.hashCode(), systemStatus2.hashCode());
        assertEquals(systemStatus1.hashCode(), systemStatus2.hashCode());

        systemStatus1.setHealthStatus(HealthStatus.OK);
        assertEquals(systemStatus, systemStatus1);
        assertNotEquals(systemStatus1, systemStatus2);
        assertEquals(systemStatus.hashCode(), systemStatus1.hashCode());
        assertNotEquals(systemStatus1.hashCode(), systemStatus2.hashCode());
    }

}