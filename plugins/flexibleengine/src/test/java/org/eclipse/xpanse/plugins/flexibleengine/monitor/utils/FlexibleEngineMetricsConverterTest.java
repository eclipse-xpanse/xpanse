package org.eclipse.xpanse.plugins.flexibleengine.monitor.utils;

import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineMonitorMetrics;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineNameSpaceKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlexibleEngineMetricsConverterTest {


    private final FlexibleEngineMetricsConverter converter = new FlexibleEngineMetricsConverter();


    @Test
    void testGetTargetMetricInfo() {
        // Setup
        final MetricInfoList metricInfoList = new MetricInfoList();
        metricInfoList.setMetricName(FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_IN);
        metricInfoList.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.name());
        metricInfoList.setUnit("bit/s");
        List<MetricInfoList> metricList = new ArrayList<>();
        metricList.add(metricInfoList);

        final MetricInfoList metricInfoList1 = new MetricInfoList();
        metricInfoList1.setMetricName(FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT);
        metricInfoList1.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.name());
        metricInfoList1.setUnit("bit/s");
        metricList.add(metricInfoList1);

        final MetricInfoList metricInfoList2 = new MetricInfoList();
        metricInfoList2.setMetricName(FlexibleEngineMonitorMetrics.CPU_UTILIZED);
        metricInfoList2.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.name());
        metricInfoList2.setUnit("%");
        metricList.add(metricInfoList2);

        // Run the test
        final MetricInfoList result1 =
                converter.getTargetMetricInfo(metricList, MonitorResourceType.CPU);
        final MetricInfoList result2 =
                converter.getTargetMetricInfo(metricList, MonitorResourceType.MEM);
        final MetricInfoList result3 =
                converter.getTargetMetricInfo(metricList,
                        MonitorResourceType.VM_NETWORK_OUTGOING);
        final MetricInfoList result4 =
                converter.getTargetMetricInfo(metricList,
                        MonitorResourceType.VM_NETWORK_INCOMING);

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result1));
        Assertions.assertEquals(result1.getMetricName(), FlexibleEngineMonitorMetrics.CPU_UTILIZED);

        Assertions.assertTrue(Objects.isNull(result2));

        Assertions.assertFalse(Objects.isNull(result3));
        Assertions.assertEquals(result3.getMetricName(),
                FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT);

        Assertions.assertFalse(Objects.isNull(result4));
        Assertions.assertEquals(result4.getMetricName(),
                FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_IN);
    }
}
