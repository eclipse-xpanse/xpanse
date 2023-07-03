/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor.enums;

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
        Assertions.assertNotEquals(MetricUnit.BYTES_PER_SECOND, MetricUnit.getByValue("Byte/s"));
        Assertions.assertNull(MetricUnit.getByValue("null"));
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
