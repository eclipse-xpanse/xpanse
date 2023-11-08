/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test of ServiceHostingType.
 */
class ServiceHostingTypeTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceHostingType.SERVICE_VENDOR, ServiceHostingType.getByValue("service-vendor"));
        assertEquals(ServiceHostingType.SELF, ServiceHostingType.getByValue("self"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceHostingType.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("service-vendor", ServiceHostingType.SERVICE_VENDOR.toValue());
        assertEquals("self", ServiceHostingType.SELF.toValue());
    }

}
