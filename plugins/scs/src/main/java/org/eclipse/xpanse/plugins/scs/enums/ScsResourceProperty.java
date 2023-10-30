/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs.enums;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;


/**
 * Enum for DeployResourceKind and SCS Resource Property.
 */
public enum ScsResourceProperty {
    SCS_VM_PROPERTY(DeployResourceKind.VM, new ScsVmProperty()),
    SCS_VOLUME_PROPERTY(DeployResourceKind.VOLUME, new ScsVolumeProperty()),
    SCS_VPC_PROPERTY(DeployResourceKind.VPC, new ScsVpcProperty()),
    SCS_PUBLIC_IP_PROPERTY(DeployResourceKind.PUBLIC_IP, new ScsPublicIpProperty()),
    SCS_SUBNET_PROPERTY(DeployResourceKind.SUBNET, new ScsSubnetProperty());

    private final DeployResourceKind resourceKind;
    private final Map<String, String> properties;

    ScsResourceProperty(DeployResourceKind resourceKind,
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
        for (ScsResourceProperty property : values()) {
            if (property.resourceKind.equals(resourceKind)) {
                return property.properties;
            }
        }
        return new HashMap<>();
    }

    /**
     * SCS cloud vm property.
     */
    static class ScsVmProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public ScsVmProperty() {
            this.put("ip", "access_ip_v4");
            this.put("image_id", "image_id");
            this.put("image_name", "image_name");
            this.put("region", "region");
        }
    }

    /**
     * SCS cloud publicIp property.
     */
    static class ScsPublicIpProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public ScsPublicIpProperty() {
            this.put("ip", "address");
        }
    }

    /**
     * SCS cloud volume property.
     */
    static class ScsVolumeProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public ScsVolumeProperty() {
            this.put("size", "size");
            this.put("type", "volume_type");
        }
    }

    /**
     * SCS cloud vpc property.
     */
    static class ScsVpcProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public ScsVpcProperty() {
            this.put("region", "region");
            this.put("mtu", "mtu");
        }
    }

    /**
     * SCS cloud Subnet property.
     */
    static class ScsSubnetProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public ScsSubnetProperty() {
            this.put("vpc", "network_id");
            this.put("subnet", "cidr");
            this.put("gateway", "gateway_ip");
        }
    }
}
