/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Scs Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs.resourcehandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceProperties;


/**
 * Enum for DeployResourceKind and SCS Resource Properties.
 */
public enum ScsTerraformResourceProperties {
    SCS_VM_PROPERTIES("openstack_compute_instance_v2", new ScsVmProperties()),
    SCS_VOLUME_PROPERTIES("openstack_blockstorage_volume_v3", new ScsVolumeProperties()),
    SCS_PUBLIC_IP_PROPERTIES("openstack_networking_floatingip_v2", new ScsPublicIpProperties()),
    SCS_VPC_PROPERTIES("openstack_networking_network_v2", new ScsVpcProperties()),
    SCS_SUBNET_PROPERTIES("openstack_networking_subnet_v2", new ScsSubnetProperties()),
    SCS_KEYPAIR_PROPERTIES("openstack_compute_keypair_v2",
            new ScsKeyPairProperties()),
    SCS_SECURITY_GROUP_PROPERTIES("openstack_networking_secgroup_v2",
            new ScsSecurityGroupProperties()),
    SCS_SECURITY_GROUP_RULE_PROPERTIES("openstack_networking_secgroup_rule_v2",
            new ScsSecurityGroupRuleProperties());

    private final String tfResourceType;
    private final DeployResourceProperties resourceProperties;

    ScsTerraformResourceProperties(String tfResourceType,
                                   DeployResourceProperties resourceProperties) {
        this.tfResourceType = tfResourceType;
        this.resourceProperties = resourceProperties;
    }


    /**
     * Get deploy resource properties by resource type in tfState.
     *
     * @param tfResourceType resource type in tfState.
     * @return DeployResourceProperties.
     */
    public static DeployResourceProperties getDeployResourceProperties(
            String tfResourceType) {
        for (ScsTerraformResourceProperties property : values()) {
            if (property.tfResourceType.equals(tfResourceType)) {
                return property.resourceProperties;
            }
        }
        return new DeployResourceProperties();
    }

    /**
     * Get set of support terraform resource types.
     *
     * @return DeployResourceProperties.
     */
    public static Set<String> getTerraformResourceTypes() {
        Set<String> types = new HashSet<>();
        for (ScsTerraformResourceProperties property : values()) {
            types.add(property.tfResourceType);
        }
        return types;
    }

    /**
     * Scs vm properties.
     */
    static class ScsVmProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.VM;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("ip", "access_ip_v4");
            map.put("image_id", "image_id");
            map.put("image_name", "image_name");
            map.put("region", "region");
            return map;
        }
    }

    /**
     * Scs volume properties.
     */
    static class ScsVolumeProperties extends DeployResourceProperties {
        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.VOLUME;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("size", "size");
            map.put("type", "volume_type");
            return map;
        }
    }

    /**
     * Scs publicIp properties.
     */
    static class ScsPublicIpProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.PUBLIC_IP;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("ip", "address");
            return map;
        }
    }

    /**
     * Scs vpc properties.
     */
    static class ScsVpcProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.VPC;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("region", "region");
            map.put("mtu", "mtu");
            return map;
        }
    }

    /**
     * Scs subnet properties.
     */
    static class ScsSubnetProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.SUBNET;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("vpc", "network_id");
            map.put("subnet", "cidr");
            map.put("gateway", "gateway_ip");
            return map;
        }
    }


    /**
     * Scs keypair properties.
     */
    static class ScsKeyPairProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.KEYPAIR;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("public_key", "public_key");
            map.put("private_key", "private_key");
            map.put("name", "name");
            return map;
        }
    }

    /**
     * Scs security group properties.
     */
    static class ScsSecurityGroupProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.SECURITY_GROUP;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("name", "name");
            map.put("description", "description");
            return map;
        }
    }

    /**
     * Scs security group role properties.
     */
    static class ScsSecurityGroupRuleProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.SECURITY_GROUP_RULE;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("direction", "direction");
            map.put("ethertype", "ethertype");
            map.put("protocol", "protocol");
            map.put("remote_ip_prefix", "remote_ip_prefix");
            map.put("port_range_min", "port_range_min");
            map.put("port_range_max", "port_range_max");
            return map;
        }
    }
}
