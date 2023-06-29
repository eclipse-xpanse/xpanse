/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ServiceRegistrationStateTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceRegistrationState.REGISTERED, ServiceRegistrationState.REGISTERED.getByValue("registered"));
        assertEquals(ServiceRegistrationState.UPDATED, ServiceRegistrationState.UPDATED.getByValue("updated"));
    }

    @Test
    void testToValue() {
        assertEquals("REGISTERED", ServiceRegistrationState.REGISTERED.toValue());
        assertEquals("UPDATED", ServiceRegistrationState.UPDATED.toValue());
    }
}
