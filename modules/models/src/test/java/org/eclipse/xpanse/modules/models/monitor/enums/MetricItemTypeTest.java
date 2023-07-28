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
 * Test of MetricItemType.
 */
class MetricItemTypeTest {

    @Test
    void testGetByValue() {
        Assertions.assertEquals(MetricItemType.VALUE, MetricItemType.VALUE.getByValue("value"));
        Assertions.assertEquals(MetricItemType.COUNT, MetricItemType.COUNT.getByValue("count"));
        Assertions.assertEquals(MetricItemType.SUM, MetricItemType.SUM.getByValue("sum"));
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> MetricItemType.SUM.getByValue("null"));
    }

    @Test
    void testToValue() {
        Assertions.assertEquals("value", MetricItemType.VALUE.toValue());
        Assertions.assertEquals("count", MetricItemType.COUNT.toValue());
        Assertions.assertEquals("sum", MetricItemType.SUM.toValue());
    }

}
