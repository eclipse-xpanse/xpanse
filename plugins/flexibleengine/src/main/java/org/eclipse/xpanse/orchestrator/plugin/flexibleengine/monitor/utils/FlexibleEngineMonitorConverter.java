/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.models.FlexibleEngineMonitorMetrics;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.models.FlexibleEngineNameSpaceKind;

/**
 * Resource conversion.
 */
@Slf4j
public class FlexibleEngineMonitorConverter {

    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public static List<String> buildMonitorMetricUrls(DeployResource resource,
            MonitorResourceType monitorResourceType) {
        List<Map<String, Object>> paramsList =
                getParameters(resource.getResourceId(), monitorResourceType);
        List<String> urlList = new ArrayList<>();
        for (Map<String, Object> paramsMap : paramsList) {
            StringBuffer url = getUri(resource.getProperties().get("region"));
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                url.append(entry.getKey())
                                .append("=").append(entry.getValue()).append("&");
            }
            url.deleteCharAt(url.length() - 1);
            urlList.add(url.toString());
        }
        return urlList;
    }


    public static Metric convertResponseToMetric(DeployResource deployResource,
            String metricName, HttpEntity entity) {
        Metric metric = new Metric();

        metric.setName(metricName);
        Map<String, String> labels = new HashMap<>();
        labels.put("id", deployResource.getResourceId());
        labels.put("name", deployResource.getName());
        metric.setLabels(labels);
        metric.setType(MetricType.GAUGE);

        /*List<Datapoint> datapointList = response.getDatapoints();
        if (!CollectionUtils.isEmpty(datapointList)) {
            datapointList.sort(Comparator.comparing(Datapoint::getTimestamp).reversed());
            MetricItem metricItem = new MetricItem();
            metricItem.setType(MetricItemType.VALUE);
            metricItem.setValue(datapointList.get(0).getAverage());
            metric.setMetrics(List.of(metricItem));
        }*/

        return metric;
    }

    private static StringBuffer getUri(String region){
        StringBuffer uri = new StringBuffer(FlexibleEngineMonitorConstants.PROTOCOL_HTTPS)
                .append(FlexibleEngineMonitorConstants.ENDPOINT_PREFIX)
                .append(region)
                .append(FlexibleEngineMonitorConstants.ENDPOINT_SUFFIX).append("/")
                .append("V1.0").append("/")
                .append(FlexibleEngineMonitorConstants.PROJECT_ID).append("/")
                .append("metric-data").append("?");
        return  uri;
    }

    private static List<Map<String, Object>> getParameters(String resourceId,
            MonitorResourceType monitorResourceType) {
        List<Map<String, Object>> paramsList = new ArrayList<>();
        if (monitorResourceType == null) {
            for (MonitorResourceType resourceType : MonitorResourceType.values()) {
                paramsList.add(getFlexibleEngineMonitorParams(resourceId, resourceType));
            }
            return paramsList;
        }
        if (monitorResourceType == MonitorResourceType.CPU) {
            paramsList =
                    List.of(getFlexibleEngineMonitorParams(resourceId, MonitorResourceType.CPU));
        } else if (monitorResourceType == MonitorResourceType.MEM) {
            paramsList =
                    List.of(getFlexibleEngineMonitorParams(resourceId, MonitorResourceType.MEM));
        }
        return paramsList;
    }

    private static Map<String, Object> getFlexibleEngineMonitorParams(String resourceId,
            MonitorResourceType monitorResourceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("namespace", FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
        params.put("metric_name", getMetricName(monitorResourceType));
        params.put("from", "0");
        params.put("to", String.valueOf(System.currentTimeMillis()));
        params.put("period", FlexibleEngineMonitorConstants.PERIOD);
        params.put("filter", FlexibleEngineMonitorConstants.AVERAGE);
        params.put("dim.0", FlexibleEngineMonitorConstants.DIM0_PREFIX + resourceId);
        return params;
    }

    private static String getMetricName(MonitorResourceType monitorResourceType) {
        String metricName = null;
        if (MonitorResourceType.CPU == monitorResourceType) {
            metricName = FlexibleEngineMonitorMetrics.CPU_UTILIZED;
        } else if (MonitorResourceType.MEM == monitorResourceType) {
            metricName = FlexibleEngineMonitorMetrics.MEM_UTILIZED;
        }
        return metricName;
    }
}
