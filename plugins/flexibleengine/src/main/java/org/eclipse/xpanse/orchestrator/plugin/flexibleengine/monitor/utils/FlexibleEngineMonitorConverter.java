/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils;

import com.huaweicloud.sdk.ces.v1.model.Datapoint;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.MetricItem;
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
        StringBuilder listMetricsBasicUrl = getQueryListMetricsBasicUrl(region, projectId);
        Map<String, String> listMetricParams = getListMetricsParams(deployResource.getResourceId());
        for (Map.Entry<String, String> entry : listMetricParams.entrySet()) {
            listMetricsBasicUrl.append(entry.getKey())
                    .append("=").append(entry.getValue()).append("&");
        }
        listMetricsBasicUrl.deleteCharAt(listMetricsBasicUrl.length() - 1);
        return listMetricsBasicUrl.toString();

    }

    private StringBuilder getQueryListMetricsBasicUrl(String region, String projectId) {
        return new StringBuilder(FlexibleEngineMonitorConstants.PROTOCOL_HTTPS)
                .append(FlexibleEngineMonitorConstants.CES_ENDPOINT_PREFIX)
                .append(region)
                .append(FlexibleEngineMonitorConstants.ENDPOINT_SUFFIX).append("/")
                .append(FlexibleEngineMonitorConstants.CES_API_VERSION).append("/")
                .append(projectId).append("/")
                .append(FlexibleEngineMonitorConstants.LISTMETRICS_PATH).append("?");
    }

    private Map<String, String> getListMetricsParams(String resourceId) {
        Map<String, String> params = new HashMap<>();
        params.put("dim.0", FlexibleEngineMonitorConstants.DIM0_PREFIX + resourceId);
        return params;
    }

    /**
     * Get list of request url for query monitor metric.
     *
     * @param resource  The deployed resource.
     * @param type      The type of monitorResource.
     * @param projectId The id of project.
     * @return Returns list of request url.
     */
    public Map<String, MonitorResourceType> buildMonitorMetricUrls(DeployResource resource,
                                                                   MonitorResourceType type,
                                                                   String projectId,
                                    FlexibleEngineNameSpaceKind flexibleEngineNameSpaceKind) {
        Map<String, MonitorResourceType> urlTypeMap = new HashMap<>();
        String region = resource.getProperties().get("region");
        String basicUrl = getQueryMetricBasicUrl(region, projectId).toString();
        if (StringUtils.isNotBlank(region)) {
            if (Objects.isNull(type)) {
                for (MonitorResourceType resourceType : MonitorResourceType.values()) {
                    StringBuilder url = new StringBuilder(basicUrl);
                    Map<String, String> paramsMap =
                            getFlexibleEngineMonitorParams(resource.getResourceId(),
                                    resourceType, flexibleEngineNameSpaceKind);
                    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                        url.append(entry.getKey())
                                .append("=").append(entry.getValue()).append("&");
                    }
                    url.deleteCharAt(url.length() - 1);
                    urlTypeMap.put(url.toString(), resourceType);
                }
            } else {
                StringBuilder url = new StringBuilder(basicUrl);
                Map<String, String> paramsMap =
                        getFlexibleEngineMonitorParams(resource.getResourceId(),
                                type, flexibleEngineNameSpaceKind);
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
     * Extract the metrics namespaces from the response and add them to the set.
     */
    public Set<String> getMetricsNamespaces(
            ListMetricsResponse listMetricsResponse) {
        return listMetricsResponse.getMetrics().stream()
                .map(MetricInfoList::getNamespace)
                .collect(Collectors.toSet());
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
                .append(FlexibleEngineMonitorConstants.PROJECT_PATH).append("?")
                .append("name=").append(region);
    }

    private Map<String, String> getFlexibleEngineMonitorParams(String resourceId,
                                                               MonitorResourceType type,
                                FlexibleEngineNameSpaceKind flexibleEngineNameSpaceKind) {
        Map<String, String> params = new HashMap<>();
        params.put("namespace", flexibleEngineNameSpaceKind.toValue());
        params.put("metric_name", getMetricName(type, flexibleEngineNameSpaceKind));
        params.put("from", String.valueOf(
                System.currentTimeMillis() - FlexibleEngineMonitorConstants.DEFAULT_DURATION));
        params.put("to", String.valueOf(System.currentTimeMillis()));
        params.put("period", String.valueOf(FlexibleEngineMonitorConstants.DEFAULT_PERIOD));
        params.put("filter", FlexibleEngineMonitorConstants.AVERAGE);
        params.put("dim.0", FlexibleEngineMonitorConstants.DIM0_PREFIX + resourceId);
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