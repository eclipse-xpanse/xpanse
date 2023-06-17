/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.utils;

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
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.orchestrator.monitor.MetricRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Resource conversion.
 */
@Slf4j
@Component
public class HuaweiCloudToXpanseDataModelConverter {

    /**
     * Build ListMetricsRequest for HuaweiCloud Monitor client.
     */
    public ListMetricsRequest buildListMetricsRequest(
            DeployResource deployResource) {
        String resourceId = deployResource.getResourceId();
        return new ListMetricsRequest()
                .withDim0(HuaweiCloudMonitorConstants.DIM0_PREFIX + resourceId);
    }

    /**
     * Build ShowMetricDataRequest for HuaweiCloud Monitor client.
     */
    public ShowMetricDataRequest buildShowMetricDataRequest(
            ResourceMetricRequest resourceMetricRequest,
            MetricInfoList metricInfoList) {
        checkNullParamAndFillValue(resourceMetricRequest);
        return new ShowMetricDataRequest()
                .withDim0(HuaweiCloudMonitorConstants.DIM0_PREFIX
                        + resourceMetricRequest.getDeployResource().getResourceId())
                .withFilter(FilterEnum.valueOf(HuaweiCloudMonitorConstants.FILTER_AVERAGE))
                .withPeriod(resourceMetricRequest.getGranularity())
                .withFrom(resourceMetricRequest.getFrom())
                .withTo(resourceMetricRequest.getTo())
                .withNamespace(metricInfoList.getNamespace())
                .withMetricName(metricInfoList.getMetricName());
    }

    /**
     * Build BatchListMetricDataRequest for HuaweiCloud Monitor client.
     */
    public BatchListMetricDataRequest buildBatchListMetricDataRequest(
            ServiceMetricRequest serviceMetricRequest,
            Map<String, List<MetricInfoList>> map) {
        checkNullParamAndFillValue(serviceMetricRequest);
        List<MetricInfo> metricInfos = new ArrayList<>();
        buildMetricsDimension(serviceMetricRequest, map, metricInfos);
        BatchListMetricDataRequestBody body = new BatchListMetricDataRequestBody();
        body.withTo(serviceMetricRequest.getTo());
        body.withFrom(serviceMetricRequest.getFrom());
        body.withFilter(HuaweiCloudMonitorConstants.FILTER_AVERAGE);
        body.withPeriod(serviceMetricRequest.getGranularity().toString());
        body.withMetrics(metricInfos);
        BatchListMetricDataRequest batchListMetricDataRequest = new BatchListMetricDataRequest();
        batchListMetricDataRequest.withBody(body);
        return batchListMetricDataRequest;
    }

    /**
     * ShowMetricDataResponse conversion to Metric.
     */
    public Metric convertShowMetricDataResponseToMetric(DeployResource deployResource,
                                                        ShowMetricDataResponse response,
                                                        MetricInfoList metricInfoList,
                                                        boolean onlyLastKnownMetric) {
        Metric metric = new Metric();
        metric.setName(metricInfoList.getMetricName());
        Map<String, String> labels = new HashMap<>();
        labels.put("id", deployResource.getResourceId());
        labels.put("name", deployResource.getName());
        metric.setLabels(labels);
        metric.setType(MetricType.GAUGE);
        if (metricInfoList.getUnit().equals("%")) {
            metric.setUnit(MetricUnit.PERCENTAGE);
        } else {
            metric.setUnit(MetricUnit.getByValue(metricInfoList.getUnit()));
        }
        if (Objects.nonNull(response) && !CollectionUtils.isEmpty(response.getDatapoints())) {
            List<Datapoint> datapointList = response.getDatapoints();
            metric.setMetrics(convertDataPointToMetricItem(datapointList, onlyLastKnownMetric));
        }
        return metric;
    }

    private List<MetricItem> convertDataPointToMetricItem(List<Datapoint> datapointList,
                                                          boolean onlyLastKnownMetric) {
        List<MetricItem> metricItems = new ArrayList<>();
        if (onlyLastKnownMetric) {
            MetricItem metricItem = new MetricItem();
            metricItem.setValue(datapointList.get(0).getAverage());
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


    /**
     * BatchListMetricDataResponse conversion to Metrics.
     */
    public List<Metric> convertBatchListMetricDataResponseToMetric(
            BatchListMetricDataResponse batchListMetricDataResponse,
            Map<String, List<MetricInfoList>> deployResourceMetricInfoMap,
            List<DeployResource> deployResources,
            boolean onlyLastKnownMetric) {
        List<Metric> metrics = new ArrayList<>();
        for (DeployResource deployResource : deployResources) {
            for (BatchMetricData batchMetricData : batchListMetricDataResponse.getMetrics()) {
                if (Objects.nonNull(batchMetricData) && deployResource.getResourceId()
                        .equals(batchMetricData.getDimensions().get(0).getValue())) {
                    List<MetricInfoList> metricInfoLists =
                            deployResourceMetricInfoMap.get(deployResource.getResourceId());
                    for (MetricInfoList metricInfoList : metricInfoLists) {
                        if (metricInfoList.getMetricName()
                                .equals(batchMetricData.getMetricName())) {
                            Metric metric = new Metric();
                            metric.setName(metricInfoList.getMetricName());
                            Map<String, String> labels = new HashMap<>();
                            labels.put("id", deployResource.getResourceId());
                            labels.put("name", deployResource.getName());
                            metric.setLabels(labels);
                            metric.setType(MetricType.GAUGE);
                            if (metricInfoList.getUnit().equals("%")) {
                                metric.setUnit(MetricUnit.PERCENTAGE);
                            } else {
                                metric.setUnit(MetricUnit.getByValue(metricInfoList.getUnit()));
                            }
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
            metricItem.setValue(
                    datapointForBatchMetrics.get(datapointForBatchMetrics.size() - 1).getAverage());
            metricItem.setType(MetricItemType.VALUE);
            metricItem.setTimeStamp(
                    datapointForBatchMetrics.get(datapointForBatchMetrics.size() - 1)
                            .getTimestamp());
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

    private void buildMetricsDimension(ServiceMetricRequest serviceMetricRequest,
                                       Map<String, List<MetricInfoList>> map,
                                       List<MetricInfo> metricInfos) {
        for (DeployResource deployResource : serviceMetricRequest.getDeployResources()) {
            List<MetricsDimension> metricsDimensions = new ArrayList<>();
            metricsDimensions.add(
                    new MetricsDimension()
                            .withName(HuaweiCloudMonitorConstants.VM_DIMENSION_NAME)
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

    private <T extends MetricRequest> void checkNullParamAndFillValue(T metricRequest) {
        if (Objects.isNull(metricRequest.getFrom())) {
            metricRequest.setFrom(System.currentTimeMillis()
                    - HuaweiCloudMonitorConstants.FIVE_MINUTES_MILLISECONDS);
        }
        if (Objects.isNull(metricRequest.getTo())) {
            metricRequest.setTo(System.currentTimeMillis());
        }
        if (Objects.isNull(metricRequest.getGranularity())) {
            metricRequest.setGranularity(HuaweiCloudMonitorConstants.PERIOD_REAL_TIME_INT);
        }
    }

}
