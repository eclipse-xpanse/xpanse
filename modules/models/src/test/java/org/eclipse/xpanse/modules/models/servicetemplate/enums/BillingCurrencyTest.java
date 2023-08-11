/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/**
 * Test of BillingCurrency.
 */
class BillingCurrencyTest {

    @Test
    void testGetByValue() {
        assertEquals(BillingCurrency.USD, BillingCurrency.USD.getByValue("usd"));
        assertEquals(BillingCurrency.EUR, BillingCurrency.EUR.getByValue("euro"));
        assertEquals(BillingCurrency.GBP, BillingCurrency.GBP.getByValue("gbp"));
        assertEquals(BillingCurrency.CAD, BillingCurrency.CAD.getByValue("cad"));
        assertEquals(BillingCurrency.DEM, BillingCurrency.DEM.getByValue("dem"));
        assertEquals(BillingCurrency.FRF, BillingCurrency.FRF.getByValue("frf"));
        assertEquals(BillingCurrency.CNY, BillingCurrency.CNY.getByValue("cny"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> BillingCurrency.CNY.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("usd", BillingCurrency.USD.toValue());
        assertEquals("euro", BillingCurrency.EUR.toValue());
        assertEquals("gbp", BillingCurrency.GBP.toValue());
        assertEquals("cad", BillingCurrency.CAD.toValue());
        assertEquals("dem", BillingCurrency.DEM.toValue());
        assertEquals("frf", BillingCurrency.FRF.toValue());
        assertEquals("cny", BillingCurrency.CNY.toValue());
    }

}
