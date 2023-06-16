/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: FlexibleEngine Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.models;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;


/**
 * Enum for DeployResourceKind and FlexibleEngine Resource Property.
 */
public enum FlexibleEngineResourceProperty {
    FlexibleEngine_VM_PROPERTY(DeployResourceKind.VM, new FlexibleEngineVmProperty()),
    FlexibleEngine_VOLUME_PROPERTY(DeployResourceKind.VOLUME, new FlexibleEngineVolumeProperty()),
    FlexibleEngine_VPC_PROPERTY(DeployResourceKind.VPC, new FlexibleEngineVpcProperty()),
    FlexibleEngine_PUBLICIP_PROPERTY(DeployResourceKind.PUBLIC_IP,
            new FlexibleEnginePublicIpProperty());

    private final DeployResourceKind resourceKind;
    private final Map<String, String> properties;

    FlexibleEngineResourceProperty(DeployResourceKind resourceKind,
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
        for (FlexibleEngineResourceProperty property : values()) {
            if (property.resourceKind.equals(resourceKind)) {
                return property.properties;
            }
        }
        return new HashMap<>();
    }

    /**
     * FlexibleEngine cloud vm property.
     */
    static class FlexibleEngineVmProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public FlexibleEngineVmProperty() {
            this.put("ip", "access_ip_v4");
            this.put("image_id", "image_id");
            this.put("image_name", "image_name");
            this.put("region", "region");
            this.put("project_id", "owner");
        }
    }

    /**
     * FlexibleEngine cloud publicIp property.
     */
    static class FlexibleEnginePublicIpProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public FlexibleEnginePublicIpProperty() {
            this.put("ip", "address");
        }
    }

    /**
     * FlexibleEngine cloud volume property.
     */
    static class FlexibleEngineVolumeProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public FlexibleEngineVolumeProperty() {
            this.put("size", "size");
            this.put("type", "volume_type");
        }
    }

    /**
     * FlexibleEngine cloud vpc property.
     */
    static class FlexibleEngineVpcProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public FlexibleEngineVpcProperty() {
            this.put("vpc", "vpc_id");
            this.put("subnet", "subnet_id");
        }
    }
}
