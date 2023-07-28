/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/**
 * Test of BillingPeriod.
 */
class BillingPeriodTest {

    @Test
    void testGetByValue() {
        assertEquals(BillingPeriod.DAILY, BillingPeriod.DAILY.getByValue("daily"));
        assertEquals(BillingPeriod.WEEKLY, BillingPeriod.WEEKLY.getByValue("weekly"));
        assertEquals(BillingPeriod.MONTHLY, BillingPeriod.MONTHLY.getByValue("monthly"));
        assertEquals(BillingPeriod.QUARTERLY, BillingPeriod.QUARTERLY.getByValue("quarterly"));
        assertEquals(BillingPeriod.YEARLY, BillingPeriod.YEARLY.getByValue("yearly"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> BillingPeriod.YEARLY.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("daily", BillingPeriod.DAILY.toValue());
        assertEquals("weekly", BillingPeriod.WEEKLY.toValue());
        assertEquals("monthly", BillingPeriod.MONTHLY.toValue());
        assertEquals("quarterly", BillingPeriod.QUARTERLY.toValue());
        assertEquals("yearly", BillingPeriod.YEARLY.toValue());
    }

}
