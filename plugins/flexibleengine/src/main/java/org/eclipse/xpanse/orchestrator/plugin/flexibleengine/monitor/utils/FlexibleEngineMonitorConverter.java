/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils;

import com.huaweicloud.sdk.ces.v1.model.Datapoint;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.MetricItem;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MetricItemType;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.models.FlexibleEngineMonitorMetrics;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.models.FlexibleEngineNameSpaceKind;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Resource conversion.
 */
@Slf4j
@Component
public class FlexibleEngineMonitorConverter {

    private static final Map<String, String> HEADER_MAP = new HashMap<>();

    /**
     * Get headers of request.
     *
     * @return Map of headers.
     */
    public Map<String, String> getHeaders() {
        HEADER_MAP.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return HEADER_MAP;
    }

    /**
     * Get request url for query monitor listMetrics.
     *
     * @param deployResource The deployed resource.
     * @param projectId      The id of project.
     * @return Return request url.
     */
    public String buildListMetricsUrl(DeployResource deployResource, String projectId) {
        String region = deployResource.getProperties().get("region");
        StringBuilder listMetricsBasicUrl =
                getQueryListMetricsBasicUrl(region, projectId, deployResource.getResourceId());
        return listMetricsBasicUrl.toString();
    }

    private StringBuilder getQueryListMetricsBasicUrl(String region, String projectId,
                                                      String resourceId) {
        return new StringBuilder(FlexibleEngineMonitorConstants.PROTOCOL_HTTPS)
                .append(FlexibleEngineMonitorConstants.CES_ENDPOINT_PREFIX)
                .append(region)
                .append(FlexibleEngineMonitorConstants.ENDPOINT_SUFFIX).append("/")
                .append(FlexibleEngineMonitorConstants.CES_API_VERSION).append("/")
                .append(projectId).append("/")
                .append(FlexibleEngineMonitorConstants.LIST_METRICS_PATH).append("?")
                .append("dim.0=")
                .append(FlexibleEngineMonitorConstants.DIM0_PREFIX).append(resourceId);
    }

    /**
     * Get list of request url for query monitor metric.
     *
     * @param resourceMetricRequest The request model to query metrics.
     * @param projectId             The id of project.
     * @return Returns list of request url.
     */
    public Map<String, MonitorResourceType> buildMonitorMetricUrls(
            ResourceMetricRequest resourceMetricRequest,
            String projectId,
            FlexibleEngineNameSpaceKind flexibleEngineNameSpaceKind) {
        Map<String, MonitorResourceType> urlTypeMap = new HashMap<>();
        DeployResource resource = resourceMetricRequest.getDeployResource();
        MonitorResourceType type = resourceMetricRequest.getMonitorResourceType();
        String region = resource.getProperties().get("region");
        String basicUrl = getQueryMetricBasicUrl(region, projectId).toString();
        if (StringUtils.isNotBlank(region)) {
            if (Objects.isNull(type)) {
                for (MonitorResourceType monitorType : MonitorResourceType.values()) {
                    StringBuilder url = new StringBuilder(basicUrl);
                    resourceMetricRequest.setMonitorResourceType(monitorType);
                    Map<String, String> paramsMap =
                            getFlexibleEngineMonitorParams(resourceMetricRequest,
                                    flexibleEngineNameSpaceKind);
                    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                        url.append(entry.getKey())
                                .append("=").append(entry.getValue()).append("&");
                    }
                    url.deleteCharAt(url.length() - 1);
                    urlTypeMap.put(url.toString(), monitorType);
                }
            } else {
                StringBuilder url = new StringBuilder(basicUrl);
                Map<String, String> paramsMap =
                        getFlexibleEngineMonitorParams(resourceMetricRequest,
                                flexibleEngineNameSpaceKind);
                for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                    url.append(entry.getKey())
                            .append("=").append(entry.getValue()).append("&");
                }
                url.deleteCharAt(url.length() - 1);
                urlTypeMap.put(url.toString(), type);
            }
        }
        return urlTypeMap;
    }

    /**
     * Convert response body to Metric object.
     *
     * @param deployResource The deployed resource.
     * @param type           MonitorResourceType.
     * @param response       ShowMetricDataResponse.
     * @return Returns Metric object.
     */
    public Metric convertResponseToMetric(DeployResource deployResource,
                                          MonitorResourceType type,
                                          ShowMetricDataResponse response,
                                          FlexibleEngineNameSpaceKind flexibleEngineNameSpaceKind) {
        Metric metric = new Metric();
        metric.setName(getMetricName(type, flexibleEngineNameSpaceKind));
        Map<String, String> labels = new HashMap<>();
        labels.put("id", deployResource.getResourceId());
        labels.put("name", deployResource.getName());
        metric.setLabels(labels);
        metric.setType(MetricType.GAUGE);
        if (Objects.nonNull(response) && !CollectionUtils.isEmpty(response.getDatapoints())) {
            List<Datapoint> datapointList = response.getDatapoints();
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

    private StringBuilder getQueryMetricBasicUrl(String region, String projectId) {
        return new StringBuilder(FlexibleEngineMonitorConstants.PROTOCOL_HTTPS)
                .append(FlexibleEngineMonitorConstants.CES_ENDPOINT_PREFIX)
                .append(region)
                .append(FlexibleEngineMonitorConstants.ENDPOINT_SUFFIX).append("/")
                .append(FlexibleEngineMonitorConstants.CES_API_VERSION).append("/")
                .append(projectId).append("/")
                .append(FlexibleEngineMonitorConstants.METRIC_PATH).append("?");

    }


    /**
     * Get url to query project info.
     *
     * @param region The region of resource.
     * @return Returns query url.
     */
    public StringBuilder buildProjectQueryUrl(String region) {
        return new StringBuilder(FlexibleEngineMonitorConstants.PROTOCOL_HTTPS)
                .append(FlexibleEngineMonitorConstants.IAM_ENDPOINT_PREFIX)
                .append(region)
                .append(FlexibleEngineMonitorConstants.ENDPOINT_SUFFIX).append("/")
                .append(FlexibleEngineMonitorConstants.IAM_API_VERSION).append("/")
                .append(FlexibleEngineMonitorConstants.PROJECTS_PATH).append("?")
                .append("name=").append(region);
    }

    private Map<String, String> getFlexibleEngineMonitorParams(
            ResourceMetricRequest resourceMetricRequest,
            FlexibleEngineNameSpaceKind flexibleEngineNameSpaceKind) {
        Map<String, String> params = new HashMap<>();
        params.put("namespace", FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
        params.put("metric_name", getMetricName(resourceMetricRequest.getMonitorResourceType(),
                flexibleEngineNameSpaceKind));
        params.put("from", String.valueOf(resourceMetricRequest.getFrom()));
        params.put("to", String.valueOf(resourceMetricRequest.getTo()));
        params.put("period", String.valueOf(resourceMetricRequest.getPeriod()));
        params.put("filter", FlexibleEngineMonitorConstants.FILTER_AVERAGE);
        params.put("dim.0", FlexibleEngineMonitorConstants.DIM0_PREFIX
                + resourceMetricRequest.getDeployResource().getResourceId());
        return params;
    }

    private String getMetricName(MonitorResourceType monitorResourceType,
                                 FlexibleEngineNameSpaceKind flexibleEngineNameSpaceKind) {
        String metricName = null;
        switch (monitorResourceType) {
            case CPU:
                if (flexibleEngineNameSpaceKind == FlexibleEngineNameSpaceKind.ECS_AGT) {
                    metricName = FlexibleEngineMonitorMetrics.CPU_USAGE;
                } else {
                    metricName = FlexibleEngineMonitorMetrics.CPU_UTILIZED;
                }
                break;
            case MEM:
                if (flexibleEngineNameSpaceKind == FlexibleEngineNameSpaceKind.ECS_AGT) {
                    metricName = FlexibleEngineMonitorMetrics.MEM_USED_IN_PERCENTAGE;
                } else {
                    metricName = FlexibleEngineMonitorMetrics.MEM_UTILIZED;
                }
                break;
            default:
                break;
        }
        return metricName;
    }
}
