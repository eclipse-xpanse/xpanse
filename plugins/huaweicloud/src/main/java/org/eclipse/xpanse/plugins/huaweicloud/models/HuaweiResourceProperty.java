/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.models;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;


/**
 * Enum for DeployResourceKind and Huawei Resource Property.
 */
public enum HuaweiResourceProperty {
    HUAWEI_VM_PROPERTY(DeployResourceKind.VM, new HuaweiVmProperty()),
    HUAWEI_VOLUME_PROPERTY(DeployResourceKind.VOLUME, new HuaweiVolumeProperty()),
    HUAWEI_VPC_PROPERTY(DeployResourceKind.VPC, new HuaweiVpcProperty()),
    HUAWEI_PUBLICIP_PROPERTY(DeployResourceKind.PUBLIC_IP, new HuaweiPublicIpProperty());

    private final DeployResourceKind resourceKind;
    private final Map<String, String> properties;

    HuaweiResourceProperty(DeployResourceKind resourceKind,
            Map<String, String> resourceProperties) {
        this.resourceKind = resourceKind;
        this.properties = resourceProperties;
    }

    /**
     * get property by resourceKind.
     *
     * @param resourceKind deployResourceKind
     * @return property map
     */
    public static Map<String, String> getProperties(DeployResourceKind resourceKind) {
        for (HuaweiResourceProperty property : values()) {
            if (property.resourceKind.equals(resourceKind)) {
                return property.properties;
            }
        }
        return new HashMap<>();
    }

    /**
     * Huawei cloud vm property.
     */
    static class HuaweiVmProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public HuaweiVmProperty() {
            this.put("ip", "access_ip_v4");
            this.put("image_id", "image_id");
            this.put("image_name", "image_name");
            this.put("region", "region");
        }
    }

    /**
     * Huawei cloud publicIp property.
     */
    static class HuaweiPublicIpProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public HuaweiPublicIpProperty() {
            this.put("ip", "address");
        }
    }

    /**
     * Huawei cloud volume property.
     */
    static class HuaweiVolumeProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public HuaweiVolumeProperty() {
            this.put("size", "size");
            this.put("type", "volume_type");
        }
    }

    /**
     * Huawei cloud vpc property.
     */
    static class HuaweiVpcProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public HuaweiVpcProperty() {
            this.put("vpc", "vpc_id");
            this.put("subnet", "subnet_id");
        }
    }
}
