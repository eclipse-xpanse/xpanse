/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of Metric. */
class MetricTest {

    private static final String name = "name";
    private static final String description = "description";
    private static final MetricType type = MetricType.GAUGE;
    private static final MonitorResourceType monitorResourceType = MonitorResourceType.CPU;
    private static final MetricUnit unit = MetricUnit.PERCENTAGE;
    private static final Map<String, String> labels = Map.of("key", "value");
    private static List<MetricItem> metrics;
    private static Metric metric;

    @BeforeEach
    void setUp() {
        MetricItem metricItem = new MetricItem();
        metricItem.setValue(100000);
        metrics = List.of(metricItem);

        metric = new Metric();
        metric.setName(name);
        metric.setDescription(description);
        metric.setType(type);
        metric.setMonitorResourceType(monitorResourceType);
        metric.setUnit(unit);
        metric.setLabels(labels);
        metric.setMetrics(metrics);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, metric.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(metric, metric);
        assertEquals(metric.hashCode(), metric.hashCode());

        Object obj = new Object();
        assertNotEquals(metric, obj);
        assertNotEquals(metric, null);
        assertNotEquals(metric.hashCode(), obj.hashCode());

        Metric metric1 = new Metric();
        Metric metric2 = new Metric();
        assertNotEquals(metric, metric1);
        assertNotEquals(metric, metric2);
        assertEquals(metric1, metric2);
        assertNotEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric.hashCode(), metric2.hashCode());
        assertEquals(metric1.hashCode(), metric2.hashCode());

        metric1.setName(name);
        assertNotEquals(metric, metric1);
        assertNotEquals(metric1, metric2);
        assertNotEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric1.hashCode(), metric2.hashCode());

        metric1.setDescription(description);
        assertNotEquals(metric, metric1);
        assertNotEquals(metric1, metric2);
        assertNotEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric1.hashCode(), metric2.hashCode());

        metric1.setType(type);
        assertNotEquals(metric, metric1);
        assertNotEquals(metric1, metric2);
        assertNotEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric1.hashCode(), metric2.hashCode());

        metric1.setMonitorResourceType(monitorResourceType);
        assertNotEquals(metric, metric1);
        assertNotEquals(metric1, metric2);
        assertNotEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric1.hashCode(), metric2.hashCode());

        metric1.setUnit(unit);
        assertNotEquals(metric, metric1);
        assertNotEquals(metric1, metric2);
        assertNotEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric1.hashCode(), metric2.hashCode());

        metric1.setLabels(labels);
        assertNotEquals(metric, metric1);
        assertNotEquals(metric1, metric2);
        assertNotEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric1.hashCode(), metric2.hashCode());

        metric1.setMetrics(metrics);
        assertEquals(metric, metric1);
        assertNotEquals(metric1, metric2);
        assertEquals(metric.hashCode(), metric1.hashCode());
        assertNotEquals(metric1.hashCode(), metric2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "Metric("
                        + "name="
                        + name
                        + ", description="
                        + description
                        + ""
                        + ", type="
                        + type
                        + ", monitorResourceType="
                        + monitorResourceType
                        + ""
                        + ", unit="
                        + unit
                        + ""
                        + ", labels="
                        + labels
                        + ", metrics="
                        + metrics
                        + ""
                        + ")";

        assertEquals(expectedString, metric.toString());
    }
}
