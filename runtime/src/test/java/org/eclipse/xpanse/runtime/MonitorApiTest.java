/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.api.MonitorApi;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.MetricItem;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for MonitorApiTest.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {XpanseApplication.class, MonitorApi.class})
@AutoConfigureMockMvc
class MonitorApiTest {

    private final Long from = System.currentTimeMillis() - 5 * 60 * 1000;

    private final Long to = System.currentTimeMillis();

    @Mock
    private Monitor mockMonitor;

    private MonitorApi monitorApiUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        monitorApiUnderTest = new MonitorApi(mockMonitor);
    }

    @Test
    void testGetMetrics() {

        // Setup
        final Metric metric = new Metric();
        metric.setName("name");
        metric.setDescription("description");
        metric.setType(MetricType.COUNTER);
        metric.setLabels(Map.ofEntries(Map.entry("value", "value")));
        final MetricItem metricItem = new MetricItem();
        metric.setMetrics(List.of(metricItem));
        final List<Metric> expectedResult = List.of(metric);

        // Configure Monitor.getMetrics(...).
        final Metric metric1 = new Metric();
        metric1.setName("name");
        metric1.setDescription("description");
        metric1.setType(MetricType.COUNTER);
        metric1.setLabels(Map.ofEntries(Map.entry("value", "value")));
        final MetricItem metricItem1 = new MetricItem();
        metric1.setMetrics(List.of(metricItem1));
        final List<Metric> metrics = List.of(metric1);
        when(mockMonitor.getMetrics(UUID.fromString("e034af0c-be03-453e-92cd-fd69acbfe526"),
                MonitorResourceType.CPU, from, to, 1)).thenReturn(metrics);

        // Run the test
        final List<Metric> result =
                monitorApiUnderTest.getMetrics("e034af0c-be03-453e-92cd-fd69acbfe526",
                        MonitorResourceType.CPU, from, to, 1);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetrics_MonitorReturnsNoItems() {
        // Setup
        when(mockMonitor.getMetrics(UUID.fromString("e034af0c-be03-453e-92cd-fd69acbfe526"),
                MonitorResourceType.CPU, from, to, 1)).thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result =
                monitorApiUnderTest.getMetrics("e034af0c-be03-453e-92cd-fd69acbfe526",
                        MonitorResourceType.CPU, from, to, 1);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
}
