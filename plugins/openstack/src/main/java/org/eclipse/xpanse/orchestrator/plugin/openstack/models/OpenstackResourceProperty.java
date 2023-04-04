/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Openstack Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.models;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;


/**
 * Enum for DeployResourceKind and Openstack Resource Property.
 */
public enum OpenstackResourceProperty {
    Openstack_VM_PROPERTY(DeployResourceKind.VM, new OpenstackVmProperty()),
    Openstack_VOLUME_PROPERTY(DeployResourceKind.VOLUME, new OpenstackVolumeProperty()),
    Openstack_VPC_PROPERTY(DeployResourceKind.VPC, new OpenstackVpcProperty()),
    Openstack_PUBLICIP_PROPERTY(DeployResourceKind.PUBLIC_IP, new OpenstackPublicIpProperty());

    private final DeployResourceKind resourceKind;
    private final Map<String, String> properties;

    OpenstackResourceProperty(DeployResourceKind resourceKind,
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
        for (OpenstackResourceProperty property : values()) {
            if (property.resourceKind.equals(resourceKind)) {
                return property.properties;
            }
        }
        return new HashMap<>();
    }

    /**
     * Openstack cloud vm property.
     */
    static class OpenstackVmProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public OpenstackVmProperty() {
            this.put("ip", "access_ip_v4");
            this.put("image_id", "image_id");
            this.put("image_name", "image_name");
            this.put("region", "region");
        }
    }

    /**
     * Openstack cloud publicIp property.
     */
    static class OpenstackPublicIpProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public OpenstackPublicIpProperty() {
            this.put("ip", "address");
        }
    }

    /**
     * Openstack cloud volume property.
     */
    static class OpenstackVolumeProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public OpenstackVolumeProperty() {
            this.put("size", "size");
            this.put("type", "volume_type");
        }
    }

    /**
     * Openstack cloud vpc property.
     */
    static class OpenstackVpcProperty extends HashMap<String, String> {

        /**
         * Init method to put property key and value.
         */
        public OpenstackVpcProperty() {
            this.put("vpc", "network_id");
            this.put("subnet", "subnetpool_id");
        }
    }
}