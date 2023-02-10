/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.SecurityGroup;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Subnet;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Vpc;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.SecurityRuleDirection;


@Slf4j
class Ocl2Hcl {

    private final Ocl ocl;

    Ocl2Hcl(Ocl ocl) {
        this.ocl = ocl;
    }

    private List<PortPair> getPortPairs(String ports) {
        List<PortPair> portPairs = new ArrayList<>();

        String[] portArray = ports.split(",");
        for (String port : portArray) {
            if (port.contains("-")) {
                String[] portPair = port.split("-");
                if (portPair.length == 2) {
                    portPairs.add(new PortPair(Integer.parseInt(portPair[0].strip()),
                            Integer.parseInt(portPair[1].strip())));
                }
            } else {
                portPairs.add(new PortPair(Integer.parseInt(port.strip()),
                        Integer.parseInt(port.strip())));
            }
        }

        return portPairs;
    }

    private String getHclVariables(String... hclVars) {
        StringBuilder hcl = new StringBuilder();
        for (String hclVar : hclVars) {
            hcl.append(String.format("""
                    variable "%s" {
                      type = string
                    }
                    """, hclVar));
        }
        return hcl.toString();
    }

    public String getHclSecurityGroupRule() {
        if (ocl == null || ocl.getNetwork() == null || ocl.getNetwork().getSecurityGroups() == null
                || ocl.getNetwork().getSecurityGroups().get(0).getRules() == null) {
            throw new IllegalArgumentException("Ocl for security group rule is invalid.");
        }

        StringBuilder hcl = new StringBuilder();

        // todo: parse ports to port_range
        for (var secGroup : ocl.getNetwork().getSecurityGroups()) {
            int index = 0;
            for (var securityRule : secGroup.getRules()) {
                var portPairs = getPortPairs(securityRule.getPorts());
                for (PortPair portPair : portPairs) {
                    //CHECKSTYLE OFF: LineLength
                    hcl.append(String.format("""

                                    resource "huaweicloud_networking_secgroup_rule" "%s_%d" {
                                      security_group_id = huaweicloud_networking_secgroup.%s.id
                                      direction         = "%s"
                                      ethertype         = "IPV4"
                                      protocol          = "%s"
                                      port_range_min    = "%s"
                                      port_range_max    = "%s"
                                      remote_ip_prefix  = "%s"
                                    }

                                    """, securityRule.getName(), index++, secGroup.getName(),
                            securityRule.getDirection().equals(SecurityRuleDirection.IN) ? "ingress"
                                    : "egress",
                            securityRule.getProtocol().toValue(),
                            portPair.getFrom(), portPair.getTo(), securityRule.getCidr()));
                    //CHECKSTYLE ON: LineLength
                }
            }
        }

        return hcl.toString();
    }

    public String getHclSecurityGroup() {
        if (ocl == null || ocl.getNetwork() == null
                || ocl.getNetwork().getSecurityGroups() == null) {
            throw new IllegalArgumentException("Ocl for security group is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        for (var secGroup : ocl.getNetwork().getSecurityGroups()) {
            hcl.append(String.format("""

                    resource "huaweicloud_networking_secgroup" "%s" {
                      name = "%s"
                    }

                    """, secGroup.getName(), secGroup.getName()));
        }

        return hcl.toString();
    }

    public String getHclVpc() {
        if (ocl == null || ocl.getNetwork() == null || ocl.getNetwork().getVpc() == null) {
            throw new IllegalArgumentException("Ocl for VPC is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        for (var vpc : ocl.getNetwork().getVpc()) {
            hcl.append(String.format("""

                    resource "huaweicloud_vpc" "%s" {
                      name = "%s"
                      cidr = "%s"
                    }

                    """, vpc.getName(), vpc.getName(), vpc.getCidr()));
        }
        return hcl.toString();
    }

    public String getHclVpcSubnet() {
        if (ocl == null || ocl.getNetwork() == null || ocl.getNetwork().getSubnets() == null) {
            throw new IllegalArgumentException("Ocl for VPC subnet is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        InetAddressValidator validator = InetAddressValidator.getInstance();
        for (var subnet : ocl.getNetwork().getSubnets()) {
            hcl.append(String.format("""

                    resource "huaweicloud_vpc_subnet" "%s" {
                      name = "%s"
                      cidr = "%s\"""", subnet.getName(), subnet.getName(), subnet.getCidr()));

            String gateway = subnet.getCidr().replaceAll("\\.\\d*/.*", ".1");
            gateway = gateway.replaceAll(":\\d*/.*", ":1");
            if (validator.isValid(gateway)) {
                hcl.append("\n  gateway_ip = \"").append(gateway).append("\"");
            }

            Optional<Vpc> vpc = ocl.referTo(subnet.getVpc(), Vpc.class);
            vpc.ifPresent(
                    value -> hcl.append("\n  vpc_id = huaweicloud_vpc.").append(value.getName())
                            .append(".id"));
            hcl.append("\n}\n\n");
        }

        return hcl.toString();
    }

    public String getHclAvailabilityZone() {
        if (ocl == null) {
            throw new IllegalArgumentException("Ocl for AvailabilityZone is invalid.");
        }
        return "\ndata \"huaweicloud_availability_zones\" \"xpanse-az\" {}\n";
    }

    public String getHclFlavor() {
        if (ocl == null) {
            throw new IllegalArgumentException("Ocl for flavor is invalid.");
        }
        return "";
    }

    public String getHclImage() {
        if (ocl == null) {
            throw new IllegalArgumentException("Ocl for image is invalid.");
        }
        return "";
    }

    public String getHclVm() {
        if (ocl == null || ocl.getCompute() == null || ocl.getCompute().getVms() == null) {
            throw new IllegalArgumentException("Ocl for vm is invalid.");
        }

        StringBuilder hcl = new StringBuilder();

        hcl.append("""
                resource "huaweicloud_compute_keypair" "xpanse-keypair" {
                  name = "xpanse-keypair"
                }""");

        for (var vm : ocl.getCompute().getVms()) {
            hcl.append(String.format("""

                    resource "huaweicloud_compute_instance" "%s" {
                      name = "%s\"""", vm.getName(), vm.getName()));

            hcl.append("\n  image_id = \"").append(vm.getImageId()).append("\"");
            hcl.append("\n  flavor_id = \"").append(vm.getType()).append("\"");

            for (var subnetPath : vm.getSubnets()) {
                Optional<Subnet> subnet = ocl.referTo(subnetPath, Subnet.class);
                subnet.ifPresent(
                        value -> hcl.append("\n  network {\n    uuid = huaweicloud_vpc_subnet.")
                                .append(value.getName()).append(".id\n  }"));
            }

            hcl.append("\n  key_pair = \"xpanse-keypair\"");
            hcl.append("\n  user_data = \"#cloud-config\\nruncmd:\\n");

            for (String command : vm.getUserData().getCommands()) {
                hcl.append("  - ").append(command).append("\\n");
            }
            hcl.append("\"");

            List<String> securityGroupList = new ArrayList<>();
            for (var secGroup : vm.getSecurityGroups()) {
                Optional<SecurityGroup> subnet = ocl.referTo(secGroup, SecurityGroup.class);
                subnet.ifPresent(value -> securityGroupList.add(value.getName()));
            }
            String securityGroupids = securityGroupList.stream()
                    .map(group -> "huaweicloud_networking_secgroup." + group + ".id")
                    .collect(Collectors.joining(","));
            hcl.append("\n  security_group_ids = [ ").append(securityGroupids).append(" ]");
            hcl.append("\n}\n\n");

            if (vm.isPublicly()) {
                //CHECKSTYLE OFF: LineLength
                hcl.append(String.format("""
                                resource "huaweicloud_vpc_eip" "%s" {
                                  publicip {
                                    type = "5_sbgp"
                                  }
                                  bandwidth {
                                    name        = "%s"
                                    size        = 5
                                    share_type  = "PER"
                                    charge_mode = "traffic"
                                  }
                                }

                                resource "huaweicloud_compute_eip_associate" "%s" {
                                  public_ip   = huaweicloud_vpc_eip.%s.address
                                  instance_id = huaweicloud_compute_instance.%s.id
                                }""", vm.getName(), vm.getName(), vm.getName(), vm.getName(),
                        vm.getName()));
                //CHECKSTYLE ON: LineLength
            }
        }
        return hcl.toString();
    }

    public String getHclStorage() {
        if (ocl == null || ocl.getStorages() == null) {
            throw new IllegalArgumentException("Ocl for storage is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        for (var storage : ocl.getStorages()) {
            hcl.append(String.format("""
                            resource "huaweicloud_evs_volume" "%s" {
                              name = "%s"
                              volume_type = "%s"
                              size = "%s"
                              """, storage.getName(), storage.getName(), storage.getType(),
                    storage.getSize()));
            // TODO: Add variable [var.availability_zone] for availability_zone
            hcl.append("\n  availability_zone = data.huaweicloud_availability_zones.xpanse-az"
                    + ".names[0]");
            hcl.append("\n}\n\n");
        }

        return hcl.toString();
    }

    public String getHcl() {
        return getHclVariables() + getHclSecurityGroup() + getHclSecurityGroupRule() + getHclVpc()
                + getHclVm() + getHclVpcSubnet() + getHclFlavor() + getHclImage()
                + getHclAvailabilityZone() + getHclStorage();
    }
}
