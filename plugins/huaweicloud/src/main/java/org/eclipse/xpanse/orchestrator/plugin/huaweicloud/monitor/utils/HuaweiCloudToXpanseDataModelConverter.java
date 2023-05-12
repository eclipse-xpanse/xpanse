/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils;

import com.huaweicloud.sdk.ces.v1.model.Datapoint;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest.FilterEnum;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.MetricItem;
import org.eclipse.xpanse.modules.monitor.enums.MetricItemType;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudMonitorMetrics;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.models.HuaweiCloudNameSpaceKind;
import org.springframework.util.CollectionUtils;

/**
 * Resource conversion.
 */
@Slf4j
public class HuaweiCloudToXpanseDataModelConverter {

    /**
     * Build ShowMetricDataRequest for HuaweiCloud Monitor client.
     */
    public static List<ShowMetricDataRequest> buildMetricDataRequest(
            DeployResource deployResource, MonitorResourceType monitorResourceType) {
        List<ShowMetricDataRequest> requestList = new ArrayList<>();
        String resourceId = deployResource.getResourceId();
        if (monitorResourceType == null) {
            for (MonitorResourceType resourceType : MonitorResourceType.values()) {
                ShowMetricDataRequest request =
                        getShowMetricDataRequest(resourceId, resourceType);
                requestList.add(request);
            }
            return requestList;
        }
        if (monitorResourceType == MonitorResourceType.CPU) {
            requestList = List.of(getShowMetricDataRequest(resourceId, MonitorResourceType.CPU));
        } else if (monitorResourceType == MonitorResourceType.MEM) {
            requestList = List.of(getShowMetricDataRequest(resourceId, MonitorResourceType.MEM));
        }
        return requestList;
    }

    private static String getNameSpace() {
        return HuaweiCloudNameSpaceKind.ECS_SYS.toValue();
    }

    private static String getMetricName(MonitorResourceType monitorResourceType) {
        String metricName = null;
        if (MonitorResourceType.CPU == monitorResourceType) {
            metricName = HuaweiCloudMonitorMetrics.CPU_UTILIZED;
        } else if (MonitorResourceType.MEM == monitorResourceType) {
            metricName = HuaweiCloudMonitorMetrics.MEM_UTILIZED;
        }
        return metricName;
    }

    /**
     * ShowMetricDataResponse conversion to Metric.
     */
    public static Metric convertResponseToMetric(DeployResource deployResource,
            ShowMetricDataRequest request,
            ShowMetricDataResponse response) {

        Metric metric = new Metric();
        metric.setName(request.getMetricName());
        Map<String, String> labels = new HashMap<>();
        labels.put("id", deployResource.getResourceId());
        labels.put("name", deployResource.getName());
        metric.setLabels(labels);
        metric.setType(MetricType.GAUGE);

        List<Datapoint> datapointList = response.getDatapoints();
        if (!CollectionUtils.isEmpty(datapointList)) {
            datapointList.sort(Comparator.comparing(Datapoint::getTimestamp).reversed());
            MetricItem metricItem = new MetricItem();
            metricItem.setType(MetricItemType.VALUE);
            metricItem.setValue(datapointList.get(0).getAverage());
            metric.setMetrics(List.of(metricItem));
        }
        return metric;
    }

    private static ShowMetricDataRequest getShowMetricDataRequest(String resourceId,
            MonitorResourceType monitorResourceType) {
        return new ShowMetricDataRequest()
                .withDim0(HuaweiCloudMonitorConstants.DIM0_PREFIX + resourceId)
                .withFilter(FilterEnum.valueOf(HuaweiCloudMonitorConstants.AVERAGE))
                .withPeriod(HuaweiCloudMonitorConstants.PERIOD)
                .withFrom(0L)
                .withTo(System.currentTimeMillis())
                .withNamespace(getNameSpace()).withMetricName(getMetricName(monitorResourceType));
    }

}
