package org.eclipse.xpanse.modules.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.MetricItem;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricItemType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.monitor.cache.MonitorMetricCacheManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {MonitorMetricStore.class, MonitorMetricCacheManager.class})
@ExtendWith(SpringExtension.class)
class MonitorMetricStoreTest {

    @Autowired
    MonitorMetricCacheManager mockMonitorMetricCacheManager;

    @Autowired
    MonitorMetricStore monitorMetricStore;

    Metric setUpMetric() {
        MetricItem metricItem = new MetricItem();
        metricItem.setTimeStamp(System.currentTimeMillis());
        metricItem.setValue(6);
        metricItem.setType(MetricItemType.VALUE);
        Metric metric = new Metric();
        metric.setMetrics(List.of(metricItem));
        Map<String, String> labels = new HashMap<>();
        labels.put("id", "resourceId");
        labels.put("name", "resourceName");
        metric.setLabels(labels);
        metric.setName("cpu_util");
        metric.setUnit(MetricUnit.PERCENTAGE);
        metric.setType(MetricType.GAUGE);
        return metric;
    }

    @Test
    void testWriteToCache() {
        monitorMetricStore.storeMonitorMetric(Csp.HUAWEI, "resourceId", MonitorResourceType.CPU,
                setUpMetric());
        Assertions.assertTrue(Objects.nonNull(
                monitorMetricStore.getMonitorMetric(Csp.HUAWEI, "resourceId",
                        MonitorResourceType.CPU)));
    }

    @Test
    void testCacheKey() {
        monitorMetricStore.storeMonitorMetric(Csp.HUAWEI, "resourceId", MonitorResourceType.CPU,
                setUpMetric());
        Assertions.assertTrue(Objects.nonNull(
                monitorMetricStore.getMonitorMetric(Csp.HUAWEI, "resourceId",
                        MonitorResourceType.CPU)));
        Assertions.assertTrue(Objects.isNull(
                monitorMetricStore.getMonitorMetric(Csp.OPENSTACK, "resourceId",
                        MonitorResourceType.CPU)));
        Assertions.assertTrue(Objects.isNull(
                monitorMetricStore.getMonitorMetric(Csp.HUAWEI, "resourceId1",
                        MonitorResourceType.CPU)));
        Assertions.assertTrue(Objects.isNull(
                monitorMetricStore.getMonitorMetric(Csp.HUAWEI, "resourceId",
                        MonitorResourceType.MEM)));
    }

    @Test
    void testCacheDeletion() {
        monitorMetricStore.storeMonitorMetric(Csp.HUAWEI, "resourceId", MonitorResourceType.CPU,
                setUpMetric());
        Assertions.assertTrue(Objects.nonNull(
                monitorMetricStore.getMonitorMetric(Csp.HUAWEI, "resourceId",
                        MonitorResourceType.CPU)));
        monitorMetricStore.deleteMonitorMetric(Csp.HUAWEI, "resourceId",
                MonitorResourceType.CPU);
        Assertions.assertTrue(Objects.isNull(
                monitorMetricStore.getMonitorMetric(Csp.HUAWEI, "resourceId",
                        MonitorResourceType.CPU)));
    }
}
