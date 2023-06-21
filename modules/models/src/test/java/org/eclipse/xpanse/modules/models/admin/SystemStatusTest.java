/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.eclipse.xpanse.modules.models.admin.enums.HealthStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of SystemStatus.
 */
class SystemStatusTest {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    public void testHealthStatusNotNull() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        Assertions.assertNotNull(systemStatus.getHealthStatus());
        Assertions.assertEquals(0, validator.validate(systemStatus).size());
    }

    @Test
    public void tsetToString() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        Assertions.assertEquals("OK", systemStatus.healthStatus.toString());
    }

    @Test
    public void testGetterAndSetter() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        assertEquals(HealthStatus.OK, systemStatus.getHealthStatus());
    }

    @Test
    public void testEqualsAndHashCode() {
        SystemStatus systemStatus1 = new SystemStatus();
        systemStatus1.setHealthStatus(HealthStatus.OK);

        SystemStatus systemStatus2 = new SystemStatus();
        systemStatus2.setHealthStatus(HealthStatus.OK);

        SystemStatus systemStatus3 = new SystemStatus();
        systemStatus3.setHealthStatus(HealthStatus.NOK);

        assertTrue(systemStatus1.equals(systemStatus2));
        assertEquals(systemStatus1.hashCode(), systemStatus2.hashCode());
        assertFalse(systemStatus1.equals(systemStatus3));
        assertNotEquals(systemStatus1.hashCode(), systemStatus3.hashCode());
    }
}
