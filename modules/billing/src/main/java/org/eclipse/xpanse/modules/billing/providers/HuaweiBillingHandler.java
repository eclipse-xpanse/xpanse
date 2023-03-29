/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.billing.providers;

import com.huaweicloud.sdk.bss.v2.model.DemandProductInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Huawei cloud resources billingHandler.
 */
public class HuaweiBillingHandler {

    /**
     * Get type code by resourceType.
     *
     * @param deployResourceEntity deployResourceEntity
     * @return rateOnDemandReq
     */
    public List<DemandProductInfo> handler(DeployResourceEntity deployResourceEntity,
            String region, Boolean unit) {
        Map<String, String> resourceType = new HashMap<>();
        resourceType.put("huaweicloud_compute_instance", "hws.service.type.ec2");
        resourceType.put("hws.service.type.ec2", "hws.resource.type.vm");
        resourceType.put("hws.resource.type.vm", "hws.service.type.ebs");
        resourceType.put("hws.service.type.ebs", "hws.resource.type.volume");
        resourceType.put("huaweicloud_vpc_eip", "hws.service.type.vpc");
        resourceType.put("hws.service.type.vpc", "hws.resource.type.ip");
        resourceType.put("hws.resource.type.ip", "hws.resource.type.bandwidth");
        resourceType.put("huaweicloud_evs_volume", "hws.service.type.ebs");
        Date createTime = deployResourceEntity.getCreateTime();
        Double startTime = Double.valueOf(createTime.getTime());
        Double endTime = Double.valueOf(new Date().getTime());
        Double time = (endTime - startTime) / (1000 * 60 * 60);
        Double value = 1.0;
        if (unit) {
            value = 1.0;
        } else {
            value = Double.parseDouble(String.format("%.1f", time));
        }
        List<DemandProductInfo> demandProductInfoList = new ArrayList<>();
        if (deployResourceEntity.getProperty().get("service_type") != null
                && deployResourceEntity.getProperty().get("create").equals("true")) {
            if (deployResourceEntity.getProperty().get("service_type")
                    .equals("huaweicloud_compute_instance")) {
                demandProductInfoList.add(
                        new DemandProductInfo()
                                .withId("ecs:" + deployResourceEntity.getId())
                                .withCloudServiceType(
                                        resourceType.get("huaweicloud_compute_instance"))
                                .withResourceType(resourceType.get(
                                        resourceType.get("huaweicloud_compute_instance")))
                                .withResourceSpec(
                                        deployResourceEntity.getProperty().get("flavor_name")
                                                + ".linux")
                                .withRegion(region)
                                .withUsageFactor("Duration")
                                .withUsageValue(value)
                                .withUsageMeasureId(4)
                                .withSubscriptionNum(1)
                );
                demandProductInfoList.add(
                        new DemandProductInfo()
                                .withId("system_disk:" + deployResourceEntity.getId())
                                .withCloudServiceType(resourceType.get(resourceType.get(
                                        resourceType.get("huaweicloud_compute_instance"))))
                                .withResourceType(
                                        resourceType.get(resourceType.get(resourceType.get(
                                                resourceType.get("huaweicloud_compute_instance")))))
                                .withResourceSpec(
                                        deployResourceEntity.getProperty().get("system_disk_type"))
                                .withRegion(region)
                                .withResourceSize(
                                        Integer.valueOf(deployResourceEntity.getProperty().get(
                                                "system_disk_size")))
                                .withSizeMeasureId(17)
                                .withUsageFactor("Duration")
                                .withUsageValue(value)
                                .withUsageMeasureId(4)
                                .withSubscriptionNum(1)
                );
                return demandProductInfoList;
            }
            String spec = null;
            String factor = null;
            int mesureId = 0;
            if (deployResourceEntity.getProperty().get("service_type")
                    .equals("huaweicloud_vpc_eip")) {
                if (deployResourceEntity.getProperty().get("bandwidth_share_type")
                        .equals("PER")) {
                    if (deployResourceEntity.getProperty().get("public_ip_type")
                            .equals("5_sbgp")) {
                        if (deployResourceEntity.getProperty().get("charge_mode")
                                .equals("traffic")) {
                            spec = "12_sbgp";
                            factor = "upflow";
                            mesureId = 10;
                        } else {
                            spec = "19_sbgp";
                            factor = "Duration";
                            mesureId = 4;
                        }
                    } else {
                        if (deployResourceEntity.getProperty().get("charge_mode")
                                .equals("traffic")) {
                            spec = "12_bgp";
                            factor = "upflow";
                            mesureId = 10;
                        } else {
                            spec = "19_bgp";
                            factor = "Duration";
                            mesureId = 4;
                        }
                    }
                }
                demandProductInfoList.add(
                        new DemandProductInfo()
                                .withId("eip_ip:" + deployResourceEntity.getId())
                                .withCloudServiceType(resourceType.get("huaweicloud_vpc_eip"))
                                .withResourceType(
                                        resourceType.get(resourceType.get("huaweicloud_vpc_eip")))
                                .withResourceSpec(
                                        deployResourceEntity.getProperty().get("public_ip_type"))
                                .withRegion(region)
                                .withUsageFactor("Duration")
                                .withUsageValue(value)
                                .withUsageMeasureId(4)
                                .withSubscriptionNum(1)
                );
                demandProductInfoList.add(
                        new DemandProductInfo()
                                .withId("eip_bandwidth:" + deployResourceEntity.getId())
                                .withCloudServiceType(resourceType.get("huaweicloud_vpc_eip"))
                                .withResourceType(
                                        resourceType.get(resourceType.get(resourceType.get(
                                                "huaweicloud_vpc_eip"))))
                                .withResourceSpec(spec)
                                .withRegion(region)
                                .withResourceSize(Integer.valueOf(
                                        deployResourceEntity.getProperty().get("bandwidth_size")))
                                .withSizeMeasureId(15)
                                .withUsageFactor(factor)
                                .withUsageValue((double) 1)
                                .withUsageMeasureId(mesureId)
                                .withSubscriptionNum(1)
                );
                return demandProductInfoList;
            }
            if (deployResourceEntity.getProperty().get("service_type")
                    .equals("huaweicloud_evs_volume")) {
                demandProductInfoList.add(
                        new DemandProductInfo()
                                .withId("volume:" + deployResourceEntity.getId())
                                .withCloudServiceType(resourceType.get("huaweicloud_evs_volume"))
                                .withResourceType(resourceType.get(
                                        resourceType.get("huaweicloud_evs_volume")))
                                .withResourceSpec(
                                        deployResourceEntity.getProperty().get("type"))
                                .withRegion(region)
                                .withResourceSize(Integer.valueOf(
                                        deployResourceEntity.getProperty().get("size")))
                                .withSizeMeasureId(17)
                                .withUsageFactor("Duration")
                                .withUsageValue(value)
                                .withUsageMeasureId(4)
                                .withSubscriptionNum(1)
                );
                return demandProductInfoList;
            }
        }
        return null;
    }

}
