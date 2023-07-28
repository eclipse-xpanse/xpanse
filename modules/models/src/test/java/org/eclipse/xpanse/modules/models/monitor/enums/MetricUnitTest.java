/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of MetricUnit.
 */
class MetricUnitTest {

    @Test
    void testGetByValue() {
        Assertions.assertEquals(MetricUnit.MB, MetricUnit.getByValue("mb"));
        Assertions.assertEquals(MetricUnit.KB, MetricUnit.getByValue("kb"));
        Assertions.assertEquals(MetricUnit.PERCENTAGE, MetricUnit.getByValue("percentage"));
        Assertions.assertEquals(MetricUnit.BITS_PER_SECOND, MetricUnit.getByValue("bit/s"));
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> MetricUnit.getByValue("Byte/s"));
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> MetricUnit.getByValue("null"));
    }

    @Test
    void testToValue() {
        Assertions.assertEquals("mb", MetricUnit.MB.toValue());
        Assertions.assertEquals("kb", MetricUnit.KB.toValue());
        Assertions.assertEquals("percentage", MetricUnit.PERCENTAGE.toValue());
        Assertions.assertEquals("bit/s", MetricUnit.BITS_PER_SECOND.toValue());
        Assertions.assertEquals("Byte/s", MetricUnit.BYTES_PER_SECOND.toValue());
    }

}
