/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils;

import static org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants.FIVE_MINUTES_MILLISECONDS;
import static org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants.PERIOD_REAL_TIME_INT;

import com.huaweicloud.sdk.ces.v1.model.Datapoint;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
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
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
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
    public String buildMonitorMetricUrl(
            ResourceMetricRequest resourceMetricRequest,
            String projectId,
            MetricInfoList metricInfo) {
        DeployResource resource = resourceMetricRequest.getDeployResource();
        String region = resource.getProperties().get("region");
        String basicUrl = getQueryMetricBasicUrl(region, projectId).toString();
        if (StringUtils.isNotBlank(region)) {
            StringBuilder url = new StringBuilder(basicUrl);
            Map<String, String> paramsMap =
                    getFlexibleEngineMonitorParams(resourceMetricRequest,
                            metricInfo);
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                url.append(entry.getKey())
                        .append("=").append(entry.getValue()).append("&");
            }
            url.deleteCharAt(url.length() - 1);
            return url.toString();
        }
        return basicUrl;
    }

    /**
     * Convert response body to Metric object.
     *
     * @param deployResource The deployed resource.
     * @param response       ShowMetricDataResponse.
     * @return Returns Metric object.
     */
    public Metric convertResponseToMetric(DeployResource deployResource,
                                          ShowMetricDataResponse response,
                                          MetricInfoList metricInfo) {
        Metric metric = new Metric();
        metric.setName(metricInfo.getMetricName());
        Map<String, String> labels = new HashMap<>();
        labels.put("id", deployResource.getResourceId());
        labels.put("name", deployResource.getName());
        metric.setLabels(labels);
        metric.setType(MetricType.GAUGE);
        if (metricInfo.getUnit().equals("%")) {
            metric.setUnit(MetricUnit.PERCENTAGE);
        } else {
            metric.setUnit(MetricUnit.getByValue(metricInfo.getUnit()));
        }
        if (Objects.nonNull(response) && !CollectionUtils.isEmpty(response.getDatapoints())) {
            List<Datapoint> datapointList = response.getDatapoints();
            datapointList.sort(Comparator.comparing(Datapoint::getTimestamp).reversed());
            metric.setMetrics(List.of(convertDataPointToMetricItem(datapointList.get(0))));
        }
        return metric;
    }

    private MetricItem convertDataPointToMetricItem(Datapoint datapoint) {
        MetricItem metricItem = new MetricItem();
        metricItem.setValue(datapoint.getAverage());
        metricItem.setType(MetricItemType.VALUE);
        metricItem.setTimeStamp(datapoint.getTimestamp());
        return metricItem;
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
            MetricInfoList metricInfo) {
        checkNullParamAndFillValue(resourceMetricRequest);
        Map<String, String> params = new HashMap<>();
        params.put("namespace", metricInfo.getNamespace());
        params.put("metric_name", metricInfo.getMetricName());
        params.put("from", String.valueOf(resourceMetricRequest.getFrom()));
        params.put("to", String.valueOf(resourceMetricRequest.getTo()));
        params.put("period", String.valueOf(resourceMetricRequest.getGranularity()));
        params.put("filter", FlexibleEngineMonitorConstants.FILTER_AVERAGE);
        params.put("dim.0", FlexibleEngineMonitorConstants.DIM0_PREFIX
                + resourceMetricRequest.getDeployResource().getResourceId());
        return params;
    }


    private void checkNullParamAndFillValue(ResourceMetricRequest resourceMetricRequest) {
        if (Objects.isNull(resourceMetricRequest.getFrom())) {
            resourceMetricRequest.setFrom(System.currentTimeMillis() - FIVE_MINUTES_MILLISECONDS);
        }
        if (Objects.isNull(resourceMetricRequest.getTo())) {
            resourceMetricRequest.setTo(System.currentTimeMillis());
        }
        if (Objects.isNull(resourceMetricRequest.getGranularity())) {
            resourceMetricRequest.setGranularity(PERIOD_REAL_TIME_INT);
        }
    }
}
