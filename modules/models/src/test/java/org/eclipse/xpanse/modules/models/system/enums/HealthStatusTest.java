/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.system.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test of HealthStatus. */
class HealthStatusTest {

    @Test
    public void testGetByValue() {
        HealthStatus okStatus = HealthStatus.OK.getByValue("OK");
        Assertions.assertEquals(HealthStatus.OK, okStatus);

        HealthStatus nokStatus = HealthStatus.NOK.getByValue("NOK");
        Assertions.assertEquals(HealthStatus.NOK, nokStatus);

        Assertions.assertThrows(
                UnsupportedEnumValueException.class, () -> HealthStatus.NOK.getByValue("null"));
    }

    @Test
    public void testToValue() {
        String okValue = HealthStatus.OK.toValue();
        Assertions.assertEquals("OK", okValue);

        String nokValue = HealthStatus.NOK.toValue();
        Assertions.assertEquals("NOK", nokValue);
    }

    @Test
    public void testEqualsAndHashCode() {
        Assertions.assertEquals(HealthStatus.OK.toString(), "OK");
        Assertions.assertEquals(HealthStatus.OK.hashCode(), HealthStatus.OK.hashCode());
    }
}
