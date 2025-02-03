/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.api.controllers.ServiceMetricsApi;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.MetricItem;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test for MonitorApiTest. */
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {XpanseApplication.class, ServiceMetricsApi.class},
        properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServiceMetricsApiTest {

    private final Long from = System.currentTimeMillis() - 5 * 60 * 1000;

    private final Long to = System.currentTimeMillis();

    private final UUID serviceId = UUID.fromString("e034af0c-be03-453e-92cd-fd69acbfe526");

    private final UUID resourceId = UUID.fromString("e034af0c-be03-453e-92cd-fd69acbfe526");

    @MockitoBean private ServiceMetricsAdapter mockServiceMetricsAdapter;

    private ServiceMetricsApi serviceMetricsApiUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceMetricsApiUnderTest = new ServiceMetricsApi(mockServiceMetricsAdapter);
    }

    @Test
    void testGetMetricsByResourceId() {

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
        when(mockServiceMetricsAdapter.getMetricsByResourceId(
                        serviceId, MonitorResourceType.CPU, from, to, 1, false))
                .thenReturn(metrics);

        // Run the test
        final List<Metric> result =
                serviceMetricsApiUnderTest.getMetrics(
                        serviceId, resourceId, MonitorResourceType.CPU, from, to, 1, false);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsByResourceIdReturnsNoItems() {
        // Setup
        when(mockServiceMetricsAdapter.getMetricsByResourceId(
                        resourceId, MonitorResourceType.CPU, from, to, 1, false))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result =
                serviceMetricsApiUnderTest.getMetrics(
                        serviceId, resourceId, MonitorResourceType.CPU, from, to, 1, false);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetMetricsByServiceId() {

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
        when(mockServiceMetricsAdapter.getMetricsByServiceId(
                        serviceId, MonitorResourceType.CPU, from, to, 1, false))
                .thenReturn(metrics);

        // Run the test
        final List<Metric> result =
                serviceMetricsApiUnderTest.getMetrics(
                        serviceId, null, MonitorResourceType.CPU, from, to, 1, false);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsByServiceIdReturnsNoItems() {
        // Setup
        when(mockServiceMetricsAdapter.getMetricsByServiceId(
                        serviceId, MonitorResourceType.CPU, from, to, 1, false))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result =
                serviceMetricsApiUnderTest.getMetrics(
                        serviceId, null, MonitorResourceType.CPU, from, to, 1, false);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
}
