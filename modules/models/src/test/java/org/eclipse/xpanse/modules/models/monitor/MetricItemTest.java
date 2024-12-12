/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of MetricItem. */
class MetricItemTest {

    public static final Map<String, String> labels = Map.of("key", "value");
    public static final MetricItemType type = MetricItemType.VALUE;
    public static final Long timeStamp = 9876543210L;
    public static final Number value = 1000000000;
    private static MetricItem metricItem;

    @BeforeEach
    void setUp() {
        metricItem = new MetricItem();
        metricItem.setLabels(labels);
        metricItem.setType(type);
        metricItem.setTimeStamp(timeStamp);
        metricItem.setValue(value);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(labels, metricItem.getLabels());
        assertEquals(type, metricItem.getType());
        assertEquals(timeStamp, metricItem.getTimeStamp());
        assertEquals(value, metricItem.getValue());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(metricItem, metricItem);
        assertEquals(metricItem.hashCode(), metricItem.hashCode());

        Object obj = new Object();
        assertNotEquals(metricItem, obj);
        assertNotEquals(metricItem, null);
        assertNotEquals(metricItem.hashCode(), obj.hashCode());

        MetricItem metricItem1 = new MetricItem();
        MetricItem metricItem2 = new MetricItem();
        assertNotEquals(metricItem, metricItem1);
        assertNotEquals(metricItem, metricItem2);
        assertEquals(metricItem1, metricItem2);
        assertNotEquals(metricItem.hashCode(), metricItem1.hashCode());
        assertNotEquals(metricItem.hashCode(), metricItem2.hashCode());
        assertEquals(metricItem1.hashCode(), metricItem2.hashCode());

        metricItem1.setLabels(labels);
        assertNotEquals(metricItem, metricItem1);
        assertNotEquals(metricItem1, metricItem2);
        assertNotEquals(metricItem.hashCode(), metricItem1.hashCode());
        assertNotEquals(metricItem1.hashCode(), metricItem2.hashCode());

        metricItem1.setType(type);
        assertNotEquals(metricItem, metricItem1);
        assertNotEquals(metricItem1, metricItem2);
        assertNotEquals(metricItem.hashCode(), metricItem1.hashCode());
        assertNotEquals(metricItem1.hashCode(), metricItem2.hashCode());

        metricItem1.setTimeStamp(timeStamp);
        assertNotEquals(metricItem, metricItem1);
        assertNotEquals(metricItem1, metricItem2);
        assertNotEquals(metricItem.hashCode(), metricItem1.hashCode());
        assertNotEquals(metricItem1.hashCode(), metricItem2.hashCode());

        metricItem1.setValue(value);
        assertEquals(metricItem, metricItem1);
        assertNotEquals(metricItem1, metricItem2);
        assertEquals(metricItem.hashCode(), metricItem1.hashCode());
        assertNotEquals(metricItem1.hashCode(), metricItem2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "MetricItem("
                        + "labels="
                        + labels
                        + ", type="
                        + type
                        + ", timeStamp="
                        + timeStamp
                        + ""
                        + ", value="
                        + value
                        + ""
                        + ")";

        assertEquals(expectedString, metricItem.toString());
    }
}
