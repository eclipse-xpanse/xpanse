/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils;

import com.huaweicloud.sdk.ces.v1.model.Datapoint;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsRequest;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest.FilterEnum;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.MetricItem;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MetricItemType;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MetricUnit;
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
     * Build ListMetricsRequest for HuaweiCloud Monitor client.
     */
    public static ListMetricsRequest buildListMetricsRequest(
            DeployResource deployResource) {
        String resourceId = deployResource.getResourceId();
        return getListMetricsRequest(resourceId);
    }

    /**
     * Build ShowMetricDataRequest for HuaweiCloud Monitor client.
     */
    public static List<ShowMetricDataRequest> buildMetricDataRequest(
            ResourceMetricRequest resourceMetricRequest,
            HuaweiCloudNameSpaceKind huaweiCloudNameSpaceKind) {
        List<ShowMetricDataRequest> requestList = new ArrayList<>();
        if (Objects.isNull(resourceMetricRequest.getMonitorResourceType())) {
            for (MonitorResourceType resourceType : MonitorResourceType.values()) {
                resourceMetricRequest.setMonitorResourceType(resourceType);
                ShowMetricDataRequest request =
                        getShowMetricDataRequest(resourceMetricRequest,
                                huaweiCloudNameSpaceKind);
                requestList.add(request);
            }
            return requestList;
        }
        requestList = List.of(getShowMetricDataRequest(resourceMetricRequest,
                huaweiCloudNameSpaceKind));

        return requestList;
    }

    private static String getNameSpace(HuaweiCloudNameSpaceKind huaweiCloudNameSpaceKind) {
        return huaweiCloudNameSpaceKind.toValue();
    }

    private static String getMetricName(MonitorResourceType monitorResourceType,
                                        HuaweiCloudNameSpaceKind huaweiCloudNameSpaceKind) {
        String metricName = null;
        switch (monitorResourceType) {
            case CPU:
                if (huaweiCloudNameSpaceKind == HuaweiCloudNameSpaceKind.ECS_SYS) {
                    metricName = HuaweiCloudMonitorMetrics.CPU_UTILIZED;
                } else {
                    metricName = HuaweiCloudMonitorMetrics.CPU_USAGEIZED;
                }
                break;
            case MEM:
                if (huaweiCloudNameSpaceKind == HuaweiCloudNameSpaceKind.ECS_SYS) {
                    metricName = HuaweiCloudMonitorMetrics.MEM_UTILIZED;
                } else {
                    metricName = HuaweiCloudMonitorMetrics.MEM_usedPercentIZED;
                }
                break;
            default:
                break;
        }
        return metricName;
    }

    /**
     * Extract the namespace from the response and add it to the set.
     */
    public static Set<String> getResponseNamespaces(
            ListMetricsResponse listMetricsResponse) {
        return listMetricsResponse.getMetrics().stream()
                .map(MetricInfoList::getNamespace)
                .collect(Collectors.toSet());
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
            if (datapointList.get(0).getUnit().equals("%")) {
                metric.setUnit(MetricUnit.PERCENTAGE);
            }
        }
        return metric;
    }

    private static ShowMetricDataRequest getShowMetricDataRequest(
            ResourceMetricRequest resourceMetricRequest,
            HuaweiCloudNameSpaceKind huaweiCloudNameSpaceKind) {
        return new ShowMetricDataRequest()
                .withDim0(HuaweiCloudMonitorConstants.DIM0_PREFIX
                        + resourceMetricRequest.getDeployResource().getResourceId())
                .withFilter(FilterEnum.valueOf(HuaweiCloudMonitorConstants.FILTER_AVERAGE))
                .withPeriod(resourceMetricRequest.getPeriod())
                .withFrom(resourceMetricRequest.getFrom())
                .withTo(resourceMetricRequest.getTo())
                .withNamespace(getNameSpace(huaweiCloudNameSpaceKind))
                .withMetricName(getMetricName(resourceMetricRequest.getMonitorResourceType(),
                        huaweiCloudNameSpaceKind));
    }

    private static ListMetricsRequest getListMetricsRequest(String resourceId) {
        return new ListMetricsRequest()
                .withDim0(HuaweiCloudMonitorConstants.DIM0_PREFIX + resourceId);
    }
}