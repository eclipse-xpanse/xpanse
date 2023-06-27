/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor.utils;

import static org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants.FOUR_HOUR_MILLISECONDS;
import static org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS;
import static org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants.ONE_MONTH_MILLISECONDS;
import static org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants.TEN_DAY_MILLISECONDS;
import static org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants.THREE_DAY_MILLISECONDS;

import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataRequestBody;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.BatchMetricData;
import com.huaweicloud.sdk.ces.v1.model.Datapoint;
import com.huaweicloud.sdk.ces.v1.model.DatapointForBatchMetric;
import com.huaweicloud.sdk.ces.v1.model.MetricInfo;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.MetricsDimension;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.MetricItem;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricItemType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.orchestrator.monitor.MetricRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineMonitorMetrics;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineNameSpaceKind;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Resource conversion.
 */
@Slf4j
@Component
public class FlexibleEngineMonitorConverter {

    private final Map<String, MonitorResourceType> metricNameToResourceTypeMap = new HashMap<>();

    /**
     * The constructor.
     * Initializes the metricNameToResourceTypeMap with the mapping of metric names to monitor
     * resource types.
     */
    public FlexibleEngineMonitorConverter() {
        metricNameToResourceTypeMap.putAll(Map.of(
                FlexibleEngineMonitorMetrics.CPU_USAGE, MonitorResourceType.CPU,
                FlexibleEngineMonitorMetrics.CPU_UTILIZED, MonitorResourceType.CPU,
                FlexibleEngineMonitorMetrics.MEM_USED_IN_PERCENTAGE, MonitorResourceType.MEM,
                FlexibleEngineMonitorMetrics.MEM_UTILIZED, MonitorResourceType.MEM,
                FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_IN,
                MonitorResourceType.VM_NETWORK_INCOMING,
                FlexibleEngineMonitorMetrics.VM_NET_BIT_SENT,
                MonitorResourceType.VM_NETWORK_INCOMING,
                FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT,
                MonitorResourceType.VM_NETWORK_OUTGOING,
                FlexibleEngineMonitorMetrics.VM_NET_BIT_RECV,
                MonitorResourceType.VM_NETWORK_OUTGOING
        ));
    }

    private static MetricItem convertDatapointForBatchMetricToMetricItem(
            DatapointForBatchMetric datapointForBatchMetric) {
        MetricItem metricItem = new MetricItem();
        metricItem.setValue(datapointForBatchMetric.getAverage());
        metricItem.setType(MetricItemType.VALUE);
        metricItem.setTimeStamp(datapointForBatchMetric.getTimestamp());
        return metricItem;
    }

    /**
     * build Batch query Monitor Metric URL.
     *
     * @param region    region name.
     * @param projectId projectId.
     */
    public static StringBuilder getBatchQueryMetricBasicUrl(String region, String projectId) {
        return new StringBuilder(FlexibleEngineMonitorConstants.PROTOCOL_HTTPS)
                .append(FlexibleEngineMonitorConstants.CES_ENDPOINT_PREFIX)
                .append(region)
                .append(FlexibleEngineMonitorConstants.ENDPOINT_SUFFIX).append("/")
                .append(FlexibleEngineMonitorConstants.CES_API_VERSION).append("/")
                .append(projectId).append("/")
                .append(FlexibleEngineMonitorConstants.BATCH_METRIC_PATH);

    }

    /**
     * build Batch query Monitor metric Request.
     *
     * @param serviceMetricRequest ServiceMetricRequest.
     * @param metricInfoMap        metricInfoMap.
     */
    public static BatchListMetricDataRequest buildBatchListMetricDataRequest(
            ServiceMetricRequest serviceMetricRequest,
            Map<String, List<MetricInfoList>> metricInfoMap) {
        List<MetricInfo> metricInfos = new ArrayList<>();
        checkNullParamAndFillValue(serviceMetricRequest);
        buildMetricsDimesion(serviceMetricRequest, metricInfoMap, metricInfos);
        BatchListMetricDataRequestBody body = new BatchListMetricDataRequestBody();
        body.withTo(serviceMetricRequest.getTo());
        body.withFrom(serviceMetricRequest.getFrom());
        body.withFilter(FlexibleEngineMonitorConstants.FILTER_AVERAGE);
        body.withPeriod(serviceMetricRequest.getGranularity().toString());
        body.withMetrics(metricInfos);
        BatchListMetricDataRequest request = new BatchListMetricDataRequest();
        request.withBody(body);
        return request;
    }

    private static void buildMetricsDimesion(ServiceMetricRequest serviceMetricRequest,
                                             Map<String, List<MetricInfoList>> map,
                                             List<MetricInfo> metricInfos) {
        for (DeployResource deployResource : serviceMetricRequest.getDeployResources()) {
            List<MetricsDimension> metricsDimensions = new ArrayList<>();
            metricsDimensions.add(
                    new MetricsDimension()
                            .withName(FlexibleEngineMonitorConstants.VM_DIMENSION_NAME)
                            .withValue(deployResource.getResourceId())
            );
            for (MetricInfoList metricInfoList : map.get(deployResource.getResourceId())) {
                metricInfos.add(
                        new MetricInfo()
                                .withNamespace(metricInfoList.getNamespace())
                                .withMetricName(metricInfoList.getMetricName())
                                .withDimensions(metricsDimensions)
                );
            }
        }
    }

    private static <T extends MetricRequest> void checkNullParamAndFillValue(T metricRequest) {
        Long from = metricRequest.getFrom();
        Long to = metricRequest.getTo();
        if (Objects.isNull(from)) {
            from = System.currentTimeMillis()
                    - FlexibleEngineMonitorConstants.FIVE_MINUTES_MILLISECONDS;
            metricRequest.setFrom(from);

        }
        if (Objects.isNull(to)) {
            to = System.currentTimeMillis();
            metricRequest.setTo(to);
        }
        if (Objects.isNull(metricRequest.getGranularity())) {
            if (to - from <= FOUR_HOUR_MILLISECONDS) {
                metricRequest.setGranularity(FlexibleEngineMonitorConstants.PERIOD_REAL_TIME_INT);
            } else if (to - from > FOUR_HOUR_MILLISECONDS && to - from <= ONE_DAY_MILLISECONDS) {
                metricRequest.setGranularity(
                        FlexibleEngineMonitorConstants.PERIOD_FIVE_MINUTES_INT);
            } else if (to - from > ONE_DAY_MILLISECONDS && to - from <= THREE_DAY_MILLISECONDS) {
                metricRequest.setGranularity(
                        FlexibleEngineMonitorConstants.PERIOD_TWENTY_MINUTES_INT);
            } else if (to - from > THREE_DAY_MILLISECONDS && to - from <= TEN_DAY_MILLISECONDS) {
                metricRequest.setGranularity(FlexibleEngineMonitorConstants.PERIOD_ONE_HOUR_INT);
            } else if (to - from > TEN_DAY_MILLISECONDS && to - from <= ONE_MONTH_MILLISECONDS) {
                metricRequest.setGranularity(FlexibleEngineMonitorConstants.PERIOD_FOUR_HOURS_INT);
            } else {
                metricRequest.setGranularity(FlexibleEngineMonitorConstants.PERIOD_ONE_DAY_INT);
            }
        }
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
                                          MetricInfoList metricInfoList,
                                          boolean onlyLastKnownMetric) {
        Metric metric =
                createMetric(metricInfoList.getMetricName(), deployResource,
                        metricInfoList);
        if (Objects.nonNull(response) && !CollectionUtils.isEmpty(response.getDatapoints())) {
            List<Datapoint> datapointList = response.getDatapoints();
            metric.setMetrics(convertDataPointToMetricItems(datapointList, onlyLastKnownMetric));
        }
        return metric;
    }

    /**
     * BatchListMetricDataResponse conversion to Metrics.
     */
    public List<Metric> convertBatchListMetricDataResponseToMetric(
            BatchListMetricDataResponse batchListMetricDataResponse,
            Map<String, List<MetricInfoList>> map,
            List<DeployResource> deployResources,
            boolean onlyLastKnownMetric) {
        List<Metric> metricList = new ArrayList<>();
        if (Objects.isNull(batchListMetricDataResponse) || CollectionUtils.isEmpty(
                batchListMetricDataResponse.getMetrics())) {
            return metricList;
        }
        for (DeployResource deployResource : deployResources) {
            for (BatchMetricData batchMetricData : batchListMetricDataResponse.getMetrics()) {
                if (Objects.nonNull(batchMetricData) && deployResource.getResourceId()
                        .equals(batchMetricData.getDimensions().get(0).getValue())) {
                    List<MetricInfoList> metricInfoLists = map.get(deployResource.getResourceId());
                    for (MetricInfoList metricInfoList : metricInfoLists) {
                        if (metricInfoList.getMetricName()
                                .equals(batchMetricData.getMetricName())) {
                            Metric metric =
                                    createMetric(metricInfoList.getMetricName(), deployResource,
                                            metricInfoList);
                            if (!CollectionUtils.isEmpty(batchMetricData.getDatapoints())) {
                                metric.setMetrics(convertBatchDataPointToMetricItems(
                                        batchMetricData.getDatapoints(), onlyLastKnownMetric));
                            }
                            metricList.add(metric);
                        }
                    }
                }
            }
        }
        return metricList;
    }

    private Metric createMetric(String metricName, DeployResource deployResource,
                                MetricInfoList metricInfo) {
        Metric metric = new Metric();
        metric.setName(metricName);
        if (metricNameToResourceTypeMap.containsKey(metricName)) {
            metric.setMonitorResourceType(metricNameToResourceTypeMap.get(metricName));
        }
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
        return metric;
    }

    private List<MetricItem> convertDataPointToMetricItems(List<Datapoint> datapointList,
                                                           boolean onlyLastKnownMetric) {
        List<MetricItem> metricItems = new ArrayList<>();
        if (onlyLastKnownMetric) {
            MetricItem metricItem = new MetricItem();
            metricItem.setValue(datapointList.get(datapointList.size() - 1).getAverage());
            metricItem.setType(MetricItemType.VALUE);
            metricItem.setTimeStamp(datapointList.get(datapointList.size() - 1).getTimestamp());
            metricItems.add(metricItem);
        } else {
            for (Datapoint datapoint : datapointList) {
                MetricItem metricItem = new MetricItem();
                metricItem.setValue(datapoint.getAverage());
                metricItem.setType(MetricItemType.VALUE);
                metricItem.setTimeStamp(datapoint.getTimestamp());
                metricItems.add(metricItem);
            }
        }
        return metricItems;
    }

    private List<MetricItem> convertBatchDataPointToMetricItems(
            List<DatapointForBatchMetric> datapointForBatchMetrics, boolean onlyLastKnownMetric) {
        List<MetricItem> metricItems = new ArrayList<>();
        if (onlyLastKnownMetric) {
            MetricItem metricItem =
                    convertDatapointForBatchMetricToMetricItem(
                            datapointForBatchMetrics.get(datapointForBatchMetrics.size() - 1));
            metricItems.add(metricItem);
        } else {
            for (DatapointForBatchMetric datapointForBatchMetric : datapointForBatchMetrics) {
                MetricItem metricItem =
                        convertDatapointForBatchMetricToMetricItem(
                                datapointForBatchMetric);
                metricItems.add(metricItem);
            }
        }
        return metricItems;
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

    /**
     * Get target MetricInfoList object by type.
     *
     * @param metrics list of MetricInfoList
     * @param type    MonitorResourceType
     * @return MetricInfoList object
     */
    public MetricInfoList getTargetMetricInfo(List<MetricInfoList> metrics,
                                              MonitorResourceType type) {
        if (MonitorResourceType.CPU.equals(type)) {
            for (MetricInfoList metric : metrics) {
                if (isAgentCpuMetrics(metric)) {
                    return metric;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(FlexibleEngineMonitorMetrics.CPU_UTILIZED);
            defaultMetricInfo.setUnit("%");
            return defaultMetricInfo;
        }
        if (MonitorResourceType.MEM.equals(type)) {
            for (MetricInfoList metric : metrics) {
                if (isAgentMemMetrics(metric)) {
                    return metric;
                }
            }
        }
        if (MonitorResourceType.VM_NETWORK_INCOMING.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentVmNetworkInMetrics(metricInfoList)) {
                    return metricInfoList;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_IN);
            defaultMetricInfo.setUnit("bit/s");
            return defaultMetricInfo;
        }
        if (MonitorResourceType.VM_NETWORK_OUTGOING.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentVmNetworkOutMetrics(metricInfoList)) {
                    return metricInfoList;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT);
            defaultMetricInfo.setUnit("bit/s");
            return defaultMetricInfo;
        }
        return null;
    }

    /**
     * Get MonitorResourceType by metricName.
     *
     * @param metricName metricName
     * @return MonitorResourceType
     */
    public MonitorResourceType getMonitorResourceTypeByMetricName(String metricName) {
        MonitorResourceType type = null;
        switch (metricName) {
            case FlexibleEngineMonitorMetrics.CPU_USAGE,
                    FlexibleEngineMonitorMetrics.CPU_UTILIZED -> type = MonitorResourceType.CPU;
            case FlexibleEngineMonitorMetrics.MEM_UTILIZED,
                    FlexibleEngineMonitorMetrics.MEM_USED_IN_PERCENTAGE -> type =
                    MonitorResourceType.MEM;
            case FlexibleEngineMonitorMetrics.VM_NET_BIT_RECV -> type =
                    MonitorResourceType.VM_NETWORK_INCOMING;
            case FlexibleEngineMonitorMetrics.VM_NET_BIT_SENT -> type =
                    MonitorResourceType.VM_NETWORK_OUTGOING;
            default -> {
            }
        }
        return type;
    }


    private boolean isAgentCpuMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.CPU_USAGE.equals(metricInfo.getMetricName());
    }

    private boolean isAgentMemMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.MEM_USED_IN_PERCENTAGE.equals(
                metricInfo.getMetricName());
    }

    private boolean isAgentVmNetworkInMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.VM_NET_BIT_SENT.equals(metricInfo.getMetricName());
    }

    private boolean isAgentVmNetworkOutMetrics(MetricInfoList metricInfo) {
        return FlexibleEngineNameSpaceKind.ECS_AGT.toValue().equals(metricInfo.getNamespace())
                && FlexibleEngineMonitorMetrics.VM_NET_BIT_RECV.equals(metricInfo.getMetricName());
    }
}
