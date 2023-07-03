/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of MetricType.
 */
class MetricTypeTest {

    @Test
    void testGetByValue() {
        Assertions.assertEquals(MetricType.COUNTER, MetricType.COUNTER.getByValue("counter"));
        Assertions.assertEquals(MetricType.GAUGE, MetricType.GAUGE.getByValue("gauge"));
        Assertions.assertEquals(MetricType.HISTOGRAM, MetricType.HISTOGRAM.getByValue("histogram"));
        Assertions.assertEquals(MetricType.SUMMARY, MetricType.SUMMARY.getByValue("summary"));
        Assertions.assertNull(MetricType.SUMMARY.getByValue("null"));
    }

    @Test
    void testToValue() {
        Assertions.assertEquals("counter", MetricType.COUNTER.toValue());
        Assertions.assertEquals("gauge", MetricType.GAUGE.toValue());
        Assertions.assertEquals("histogram", MetricType.HISTOGRAM.toValue());
        Assertions.assertEquals("summary", MetricType.SUMMARY.toValue());
    }

}
