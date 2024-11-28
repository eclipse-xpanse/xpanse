/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ServiceTemplateRegistrationStateTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceTemplateRegistrationState.IN_REVIEW,
                ServiceTemplateRegistrationState.getByValue("in-review"));
        assertEquals(ServiceTemplateRegistrationState.APPROVED,
                ServiceTemplateRegistrationState.getByValue("approved"));
        assertEquals(ServiceTemplateRegistrationState.REJECTED,
                ServiceTemplateRegistrationState.getByValue("rejected"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceTemplateRegistrationState.getByValue("error_value"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceTemplateRegistrationState.getByValue(null));

    }

    @Test
    void testToValue() {
        assertEquals("in-review", ServiceTemplateRegistrationState.IN_REVIEW.toValue());
        assertEquals("approved", ServiceTemplateRegistrationState.APPROVED.toValue());
        assertEquals("rejected", ServiceTemplateRegistrationState.REJECTED.toValue());
    }
}
