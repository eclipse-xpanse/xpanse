/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor.providers;

import com.huaweicloud.sdk.ces.v1.model.Datapoint;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.service.DataValue;
import org.eclipse.xpanse.modules.models.service.MonitorDataResponse;
import org.eclipse.xpanse.modules.monitor.providers.model.HuaweiMonitorResource;
import org.springframework.stereotype.Component;

/**
 * Resource conversion.
 */
@Component
@Slf4j
public class MonitorResourceHandler {

    /**
     * resource conversion.
     *
     * @return HuaweiMonitorResource.
     */
    public HuaweiMonitorResource handler(DeployResourceEntity deployResourceEntity, String fromTime,
            String toTime) {
        HuaweiMonitorResource huaweiMonitorResource = new HuaweiMonitorResource();
        huaweiMonitorResource.setDim0("instance_id," + deployResourceEntity.getResourceId());
        huaweiMonitorResource.setRegion(deployResourceEntity.getProperty().get("region"));
        huaweiMonitorResource.setPeriod(300);
        huaweiMonitorResource.setFilter("average");
        if (fromTime == null || toTime == null) {
            huaweiMonitorResource.setFrom(String.valueOf(new Date().getTime() - 5 * 60 * 1000));
            huaweiMonitorResource.setTo(String.valueOf(new Date().getTime()));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                huaweiMonitorResource.setFrom(String.valueOf(sdf.parse(fromTime).getTime()));
                huaweiMonitorResource.setTo(String.valueOf(sdf.parse(toTime).getTime()));
            } catch (ParseException e) {
                log.error("Parse exception.", e.getMessage());
            }
        }
        return huaweiMonitorResource;
    }

    /**
     * resource conversion.
     *
     * @return MonitorDataResponse.
     */
    public MonitorDataResponse convert(ShowMetricDataResponse response, String id) {
        MonitorDataResponse monitorDataResponse = new MonitorDataResponse();
        List<DataValue> dataValues = new ArrayList<>();
        DataValue dataValue = new DataValue();
        for (Datapoint datapoint : response.getDatapoints()) {
            dataValue.setAverage(datapoint.getAverage());
            dataValue.setUnit("%");
            dataValues.add(dataValue);
        }
        monitorDataResponse.setResourceId(id);
        monitorDataResponse.setDataValues(dataValues);
        return monitorDataResponse;
    }

}
