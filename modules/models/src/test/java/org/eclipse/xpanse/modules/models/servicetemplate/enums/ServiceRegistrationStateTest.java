/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ServiceRegistrationStateTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceRegistrationState.APPROVED,
                ServiceRegistrationState.getByValue("approved"));
        assertEquals(ServiceRegistrationState.APPROVAL_PENDING,
                ServiceRegistrationState.getByValue("approval pending"));
        assertEquals(ServiceRegistrationState.REJECTED,
                ServiceRegistrationState.getByValue("rejected"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceRegistrationState.getByValue("error_value"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceRegistrationState.getByValue(null));
    }

    @Test
    void testToValue() {
        assertEquals("approved", ServiceRegistrationState.APPROVED.toValue());
        assertEquals("approval pending", ServiceRegistrationState.APPROVAL_PENDING.toValue());
        assertEquals("rejected", ServiceRegistrationState.REJECTED.toValue());
    }
}
