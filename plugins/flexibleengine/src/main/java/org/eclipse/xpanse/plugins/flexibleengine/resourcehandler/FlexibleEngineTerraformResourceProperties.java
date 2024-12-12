/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: FlexibleEngine Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.resourcehandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceProperties;

/** Enum for resource type in tfState and FlexibleEngineResourceProperties. */
public enum FlexibleEngineTerraformResourceProperties {
    FLEXIBLE_ENGINE_VM_PROPERTIES(
            "flexibleengine_compute_instance_v2", new FlexibleEngineVmProperties()),
    FLEXIBLE_ENGINE_VOLUME_PROPERTIES(
            "flexibleengine_blockstorage_volume_v2", new FlexibleEngineVolumeProperties()),
    FLEXIBLE_ENGINE_PUBLIC_IP_PROPERTIES(
            "flexibleengine_vpc_eip", new FlexibleEnginePublicIpProperties()),
    FLEXIBLE_ENGINE_VPC_PROPERTIES("flexibleengine_vpc_v1", new FlexibleEngineVpcProperties()),
    FLEXIBLE_ENGINE_SUBNET_PROPERTIES(
            "flexibleengine_vpc_subnet_v1", new FlexibleEngineSubnetProperties()),
    FLEXIBLE_ENGINE_KEYPAIR_PROPERTIES(
            "flexibleengine_compute_keypair_v2", new FlexibleEngineKeyPairProperties()),
    FLEXIBLE_ENGINE_SECURITY_GROUP_PROPERTIES(
            "flexibleengine_networking_secgroup_v2", new FlexibleEngineSecurityGroupProperties()),
    FLEXIBLE_ENGINE_SECURITY_GROUP_RULE_PROPERTIES(
            "flexibleengine_networking_secgroup_rule_v2",
            new FlexibleEngineSecurityGroupRuleProperties());

    private final String tfResourceType;
    private final DeployResourceProperties resourceProperties;

    FlexibleEngineTerraformResourceProperties(
            String tfResourceType, DeployResourceProperties resourceProperties) {
        this.tfResourceType = tfResourceType;
        this.resourceProperties = resourceProperties;
    }

    /**
     * Get deploy resource properties by resourceKind.
     *
     * @param tfResourceType resource type in tfState.
     * @return DeployResourceProperties.
     */
    public static DeployResourceProperties getDeployResourceProperties(String tfResourceType) {
        for (FlexibleEngineTerraformResourceProperties property : values()) {
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
        for (FlexibleEngineTerraformResourceProperties property : values()) {
            types.add(property.tfResourceType);
        }
        return types;
    }

    /** FlexibleEngine vm properties. */
    static class FlexibleEngineVmProperties extends DeployResourceProperties {

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
            map.put("project_id", "owner");
            return map;
        }
    }

    /** FlexibleEngine volume properties. */
    static class FlexibleEngineVolumeProperties extends DeployResourceProperties {
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

    /** FlexibleEngine publicIp properties. */
    static class FlexibleEnginePublicIpProperties extends DeployResourceProperties {

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

    /** FlexibleEngine vpc properties. */
    static class FlexibleEngineVpcProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.VPC;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("cidr", "cidr");
            map.put("region", "region");
            return map;
        }
    }

    /** FlexibleEngine subnet properties. */
    static class FlexibleEngineSubnetProperties extends DeployResourceProperties {

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

    /** FlexibleEngine keypair properties. */
    static class FlexibleEngineKeyPairProperties extends DeployResourceProperties {

        @Override
        public DeployResourceKind getResourceKind() {
            return DeployResourceKind.KEYPAIR;
        }

        @Override
        public Map<String, String> getResourceProperties() {
            Map<String, String> map = new HashMap<>();
            map.put("name", "name");
            map.put("public_key", "public_key");
            map.put("private_key", "private_key_path");
            return map;
        }
    }

    /** FlexibleEngine security group properties. */
    static class FlexibleEngineSecurityGroupProperties extends DeployResourceProperties {

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

    /** FlexibleEngine security group role properties. */
    static class FlexibleEngineSecurityGroupRuleProperties extends DeployResourceProperties {

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
