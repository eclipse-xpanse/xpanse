/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.modules.models.service.register.enums.BillingCurrency;
import org.eclipse.xpanse.modules.models.service.register.enums.BillingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of Billing.
 */
class BillingTest {

    private static final String model = "model";
    private static final BillingPeriod period = BillingPeriod.DAILY;
    private static final BillingCurrency currency = BillingCurrency.USD;
    private static Billing billing;

    @BeforeEach
    void setUp() {
        billing = new Billing();
        billing.setModel(model);
        billing.setPeriod(period);
        billing.setCurrency(currency);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(model, billing.getModel());
        assertEquals(period, billing.getPeriod());
        assertEquals(currency, billing.getCurrency());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(billing, billing);
        assertEquals(billing.hashCode(), billing.hashCode());

        Object obj = new Object();
        assertNotEquals(billing, obj);
        assertNotEquals(billing, null);
        assertNotEquals(billing.hashCode(), obj.hashCode());

        Billing billing1 = new Billing();
        Billing billing2 = new Billing();
        assertNotEquals(billing, billing1);
        assertNotEquals(billing, billing2);
        assertEquals(billing1, billing2);
        assertNotEquals(billing.hashCode(), billing1.hashCode());
        assertNotEquals(billing.hashCode(), billing2.hashCode());
        assertEquals(billing1.hashCode(), billing2.hashCode());

        billing1.setModel(model);
        assertNotEquals(billing, billing1);
        assertNotEquals(billing1, billing2);
        assertNotEquals(billing.hashCode(), billing1.hashCode());
        assertNotEquals(billing1.hashCode(), billing2.hashCode());

        billing1.setPeriod(period);
        assertNotEquals(billing, billing1);
        assertNotEquals(billing1, billing2);
        assertNotEquals(billing.hashCode(), billing1.hashCode());
        assertNotEquals(billing1.hashCode(), billing2.hashCode());

        billing1.setCurrency(currency);
        assertEquals(billing, billing1);
        assertNotEquals(billing1, billing2);
        assertEquals(billing.hashCode(), billing1.hashCode());
        assertNotEquals(billing1.hashCode(), billing2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "Billing(" +
                "model=" + model +
                ", period=" + period + "" +
                ", currency=" + currency +
                ")";

        assertEquals(expectedString, billing.toString());
    }

}
