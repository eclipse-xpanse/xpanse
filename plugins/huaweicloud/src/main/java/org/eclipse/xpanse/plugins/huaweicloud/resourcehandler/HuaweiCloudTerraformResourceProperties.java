/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.resourcehandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceProperties;

/** Enum for resource type in tfState and HuaweiCloudResourceProperties. */
public enum HuaweiCloudTerraformResourceProperties {
    HUAWEI_CLOUD_VM_PROPERTIES("huaweicloud_compute_instance", new HuaweiVmProperties()),
    HUAWEI_CLOUD_VOLUME_PROPERTIES("huaweicloud_evs_volume", new HuaweiVolumeProperties()),
    HUAWEI_CLOUD_PUBLIC_IP_PROPERTIES("huaweicloud_vpc_eip", new HuaweiPublicIpProperties()),
    HUAWEI_CLOUD_VPC_PROPERTIES("huaweicloud_vpc", new HuaweiVpcProperties()),
    HUAWEI_CLOUD_SUBNET_PROPERTIES("huaweicloud_vpc_subnet", new HuaweiSubnetProperties()),
    HUAWEI_CLOUD_KEYPAIR_PROPERTIES("huaweicloud_kps_keypair", new HuaweiKeyPairProperties()),
    HUAWEI_CLOUD_SECURITY_GROUP_PROPERTIES(
            "huaweicloud_networking_secgroup", new HuaweiSecurityGroupProperties()),
    HUAWEI_CLOUD_SECURITY_GROUP_RULE_PROPERTIES(
            "huaweicloud_networking_secgroup_rule", new HuaweiSecurityGroupRuleProperties());

    private final String tfResourceType;
    private final DeployResourceProperties resourceProperties;

    HuaweiCloudTerraformResourceProperties(
            String tfResourceType, DeployResourceProperties resourceProperties) {
        this.tfResourceType = tfResourceType;
        this.resourceProperties = resourceProperties;
    }

    /**
     * Get deploy resource properties by resource type in tfState.
     *
     * @param tfResourceType resource type in tfState.
     * @return DeployResourceProperties.
     */
    public static DeployResourceProperties getDeployResourceProperties(String tfResourceType) {
        for (HuaweiCloudTerraformResourceProperties property : values()) {
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
        for (HuaweiCloudTerraformResourceProperties property : values()) {
            types.add(property.tfResourceType);
        }
        return types;
    }

    /** Huawei cloud vm properties. */
    static class HuaweiVmProperties extends DeployResourceProperties {

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

    /** Huawei cloud volume properties. */
    static class HuaweiVolumeProperties extends DeployResourceProperties {
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

    /** Huawei cloud publicIp properties. */
    static class HuaweiPublicIpProperties extends DeployResourceProperties {

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

    /** Huawei cloud vpc properties. */
    static class HuaweiVpcProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.VPC;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("region", "region");
            return map;
        }
    }

    /** Huawei cloud subnet properties. */
    static class HuaweiSubnetProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.SUBNET;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("vpc", "vpc_id");
            map.put("subnet", "cidr");
            map.put("gateway", "gateway_ip");
            return map;
        }
    }

    /** Huawei cloud keypair properties. */
    static class HuaweiKeyPairProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.KEYPAIR;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("name", "name");
            map.put("public_key", "public_key");
            map.put("private_key", "key_file");
            return map;
        }
    }

    /** Huawei cloud security group properties. */
    static class HuaweiSecurityGroupProperties extends DeployResourceProperties {

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

    /** Huawei cloud security group role properties. */
    static class HuaweiSecurityGroupRuleProperties extends DeployResourceProperties {

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
