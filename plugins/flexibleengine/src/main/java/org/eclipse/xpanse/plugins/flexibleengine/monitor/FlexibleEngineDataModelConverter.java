/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor;

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
import com.huaweicloud.sdk.ces.v1.model.ListMetricsRequest;
import com.huaweicloud.sdk.ces.v1.model.MetricInfo;
import com.huaweicloud.sdk.ces.v1.model.MetricInfoList;
import com.huaweicloud.sdk.ces.v1.model.MetricsDimension;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest.FilterEnum;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.MetricItem;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricItemType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.orchestrator.monitor.MetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorMetrics;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Resource conversion. */
@Slf4j
@Component
public class FlexibleEngineDataModelConverter {

    private final Map<String, MonitorResourceType> metricNameToResourceTypeMap = new HashMap<>();

    /**
     * The constructor. Initializes the metricNameToResourceTypeMap with the mapping of metric names
     * to monitor resource types.
     */
    public FlexibleEngineDataModelConverter() {
        metricNameToResourceTypeMap.putAll(
                Map.of(
                        FlexibleEngineMonitorMetrics.CPU_USAGE,
                        MonitorResourceType.CPU,
                        FlexibleEngineMonitorMetrics.CPU_UTILIZED,
                        MonitorResourceType.CPU,
                        FlexibleEngineMonitorMetrics.MEM_USED_IN_PERCENTAGE,
                        MonitorResourceType.MEM,
                        FlexibleEngineMonitorMetrics.MEM_UTILIZED,
                        MonitorResourceType.MEM,
                        FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_IN,
                        MonitorResourceType.VM_NETWORK_INCOMING,
                        FlexibleEngineMonitorMetrics.VM_NET_BIT_SENT,
                        MonitorResourceType.VM_NETWORK_INCOMING,
                        FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT,
                        MonitorResourceType.VM_NETWORK_OUTGOING,
                        FlexibleEngineMonitorMetrics.VM_NET_BIT_RECV,
                        MonitorResourceType.VM_NETWORK_OUTGOING));
    }

    /** Build ListMetricsRequest for FlexibleEngine Monitor client. */
    public ListMetricsRequest buildListMetricsRequest(DeployResource deployResource) {
        String resourceId = deployResource.getResourceId();
        return new ListMetricsRequest()
                .withDim0(FlexibleEngineMonitorConstants.DIM0_PREFIX + resourceId);
    }

    /** Build ShowMetricDataRequest for FlexibleEngine Monitor client. */
    public ShowMetricDataRequest buildShowMetricDataRequest(
            ResourceMetricsRequest resourceMetricRequest, MetricInfoList metricInfoList) {
        checkNullParamAndFillValue(resourceMetricRequest);
        return new ShowMetricDataRequest()
                .withDim0(
                        FlexibleEngineMonitorConstants.DIM0_PREFIX
                                + resourceMetricRequest.getDeployResource().getResourceId())
                .withFilter(FilterEnum.valueOf(FlexibleEngineMonitorConstants.FILTER_AVERAGE))
                .withPeriod(resourceMetricRequest.getGranularity())
                .withFrom(resourceMetricRequest.getFrom())
                .withTo(resourceMetricRequest.getTo())
                .withNamespace(metricInfoList.getNamespace())
                .withMetricName(metricInfoList.getMetricName());
    }

    /** Build BatchListMetricDataRequest for FlexibleEngine Monitor client. */
    public BatchListMetricDataRequest buildBatchListMetricDataRequest(
            ServiceMetricsRequest serviceMetricRequest, Map<String, List<MetricInfoList>> map) {
        checkNullParamAndFillValue(serviceMetricRequest);
        List<MetricInfo> metricInfos = new ArrayList<>();
        buildMetricsDimension(serviceMetricRequest, map, metricInfos);
        BatchListMetricDataRequestBody body = new BatchListMetricDataRequestBody();
        body.withTo(serviceMetricRequest.getTo());
        body.withFrom(serviceMetricRequest.getFrom());
        body.withFilter(FlexibleEngineMonitorConstants.FILTER_AVERAGE);
        body.withPeriod(serviceMetricRequest.getGranularity().toString());
        body.withMetrics(metricInfos);
        BatchListMetricDataRequest batchListMetricDataRequest = new BatchListMetricDataRequest();
        batchListMetricDataRequest.withBody(body);
        return batchListMetricDataRequest;
    }

    /** ShowMetricDataResponse conversion to Metric. */
    public Metric convertShowMetricDataResponseToMetric(
            DeployResource deployResource,
            ShowMetricDataResponse response,
            MetricInfoList metricInfoList,
            boolean onlyLastKnownMetric) {
        Metric metric =
                createMetric(metricInfoList.getMetricName(), deployResource, metricInfoList);
        if (Objects.nonNull(response) && !CollectionUtils.isEmpty(response.getDatapoints())) {
            List<Datapoint> datapointList = response.getDatapoints();
            metric.setMetrics(convertDataPointToMetricItem(datapointList, onlyLastKnownMetric));
        }
        return metric;
    }

    private List<MetricItem> convertDataPointToMetricItem(
            List<Datapoint> datapointList, boolean onlyLastKnownMetric) {
        List<MetricItem> metricItems = new ArrayList<>();
        if (onlyLastKnownMetric) {
            MetricItem metricItem = new MetricItem();
            metricItem.setValue(datapointList.getFirst().getAverage());
            metricItem.setType(MetricItemType.VALUE);
            metricItem.setTimeStamp(datapointList.getLast().getTimestamp());
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

    /** BatchListMetricDataResponse conversion to Metrics. */
    public List<Metric> convertBatchListMetricDataResponseToMetric(
            BatchListMetricDataResponse batchListMetricDataResponse,
            Map<String, List<MetricInfoList>> deployResourceMetricInfoMap,
            List<DeployResource> deployResources,
            boolean onlyLastKnownMetric) {
        List<Metric> metrics = new ArrayList<>();
        for (DeployResource deployResource : deployResources) {
            for (BatchMetricData batchMetricData : batchListMetricDataResponse.getMetrics()) {
                if (Objects.nonNull(batchMetricData)
                        && deployResource
                                .getResourceId()
                                .equals(batchMetricData.getDimensions().getFirst().getValue())) {
                    List<MetricInfoList> metricInfoLists =
                            deployResourceMetricInfoMap.get(deployResource.getResourceId());
                    for (MetricInfoList metricInfoList : metricInfoLists) {
                        if (metricInfoList
                                .getMetricName()
                                .equals(batchMetricData.getMetricName())) {
                            Metric metric =
                                    createMetric(
                                            metricInfoList.getMetricName(),
                                            deployResource,
                                            metricInfoList);
                            if (!CollectionUtils.isEmpty(batchMetricData.getDatapoints())) {
                                List<DatapointForBatchMetric> datapointForBatchMetrics =
                                        batchMetricData.getDatapoints();
                                List<MetricItem> metricItems =
                                        convertDatapointForBatchMetricToMetricItem(
                                                datapointForBatchMetrics, onlyLastKnownMetric);
                                metric.setMetrics(metricItems);
                            }
                            metrics.add(metric);
                        }
                    }
                }
            }
        }
        return metrics;
    }

    private List<MetricItem> convertDatapointForBatchMetricToMetricItem(
            List<DatapointForBatchMetric> datapointForBatchMetrics, boolean onlyLastKnownMetric) {
        List<MetricItem> metricItems = new ArrayList<>();
        if (onlyLastKnownMetric) {
            MetricItem metricItem = new MetricItem();
            metricItem.setValue(datapointForBatchMetrics.getLast().getAverage());
            metricItem.setType(MetricItemType.VALUE);
            metricItem.setTimeStamp(datapointForBatchMetrics.getLast().getTimestamp());
            metricItems.add(metricItem);
        } else {
            for (DatapointForBatchMetric datapointForBatchMetric : datapointForBatchMetrics) {
                MetricItem metricItem = new MetricItem();
                metricItem.setValue(datapointForBatchMetric.getAverage());
                metricItem.setType(MetricItemType.VALUE);
                metricItem.setTimeStamp(datapointForBatchMetric.getTimestamp());
                metricItems.add(metricItem);
            }
        }
        return metricItems;
    }

    private Metric createMetric(
            String metricName, DeployResource deployResource, MetricInfoList metricInfo) {
        Metric metric = new Metric();
        metric.setName(metricName);
        if (metricNameToResourceTypeMap.containsKey(metricName)) {
            metric.setMonitorResourceType(metricNameToResourceTypeMap.get(metricName));
        }
        Map<String, String> labels = new HashMap<>();
        labels.put("id", deployResource.getResourceId());
        labels.put("name", deployResource.getResourceName());
        metric.setLabels(labels);
        metric.setType(MetricType.GAUGE);
        if (metricInfo.getUnit().equals("%")) {
            metric.setUnit(MetricUnit.PERCENTAGE);
        } else {
            metric.setUnit(MetricUnit.getByValue(metricInfo.getUnit()));
        }
        return metric;
    }

    private void buildMetricsDimension(
            ServiceMetricsRequest serviceMetricRequest,
            Map<String, List<MetricInfoList>> map,
            List<MetricInfo> metricInfos) {
        for (DeployResource deployResource : serviceMetricRequest.getDeployResources()) {
            List<MetricsDimension> metricsDimensions = new ArrayList<>();
            metricsDimensions.add(
                    new MetricsDimension()
                            .withName(FlexibleEngineMonitorConstants.VM_DIMENSION_NAME)
                            .withValue(deployResource.getResourceId()));
            for (MetricInfoList metricInfoList : map.get(deployResource.getResourceId())) {
                metricInfos.add(
                        new MetricInfo()
                                .withNamespace(metricInfoList.getNamespace())
                                .withMetricName(metricInfoList.getMetricName())
                                .withDimensions(metricsDimensions));
            }
        }
    }

    private <T extends MetricsRequest> void checkNullParamAndFillValue(T metricRequest) {

        if (Objects.isNull(metricRequest.getFrom())) {
            metricRequest.setFrom(
                    System.currentTimeMillis()
                            - FlexibleEngineMonitorConstants.FIVE_MINUTES_MILLISECONDS);
        }
        if (Objects.isNull(metricRequest.getTo())) {
            metricRequest.setTo(System.currentTimeMillis());
        }

        if (Objects.isNull(metricRequest.getGranularity())) {
            Long from = metricRequest.getFrom();
            Long to = metricRequest.getTo();
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
     * Get target MetricInfoList object by type.
     *
     * @param metrics list of MetricInfoList
     * @param type MonitorResourceType
     * @return MetricInfoList object
     */
    public MetricInfoList getTargetMetricInfo(
            List<MetricInfoList> metrics, MonitorResourceType type) {
        if (MonitorResourceType.CPU.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentCpuMetrics(metricInfoList)) {
                    return metricInfoList;
                }
            }
            MetricInfoList defaultMetricInfo = new MetricInfoList();
            defaultMetricInfo.setNamespace(FlexibleEngineNameSpaceKind.ECS_SYS.toValue());
            defaultMetricInfo.setMetricName(FlexibleEngineMonitorMetrics.CPU_UTILIZED);
            defaultMetricInfo.setUnit("%");
            return defaultMetricInfo;
        }
        if (MonitorResourceType.MEM.equals(type)) {
            for (MetricInfoList metricInfoList : metrics) {
                if (isAgentMemMetrics(metricInfoList)) {
                    return metricInfoList;
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
                            FlexibleEngineMonitorMetrics.CPU_UTILIZED ->
                    type = MonitorResourceType.CPU;
            case FlexibleEngineMonitorMetrics.MEM_UTILIZED,
                            FlexibleEngineMonitorMetrics.MEM_USED_IN_PERCENTAGE ->
                    type = MonitorResourceType.MEM;
            case FlexibleEngineMonitorMetrics.VM_NET_BIT_RECV,
                            FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_IN ->
                    type = MonitorResourceType.VM_NETWORK_INCOMING;
            case FlexibleEngineMonitorMetrics.VM_NET_BIT_SENT,
                            FlexibleEngineMonitorMetrics.VM_NETWORK_BANDWIDTH_OUT ->
                    type = MonitorResourceType.VM_NETWORK_OUTGOING;
            default -> {}
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
