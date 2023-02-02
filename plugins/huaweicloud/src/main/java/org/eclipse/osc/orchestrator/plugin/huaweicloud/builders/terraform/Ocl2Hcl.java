/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Security;
import org.eclipse.osc.modules.ocl.loader.data.models.Subnet;
import org.eclipse.osc.modules.ocl.loader.data.models.Vpc;


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
                portPairs.add(
                        new PortPair(Integer.parseInt(port.strip()),
                                Integer.parseInt(port.strip())));
            }
        }

        return portPairs;
    }

    private String getHclVariables(String... hclVars) {
        StringBuilder hcl = new StringBuilder();
        for (String hclVar : hclVars) {
            hcl.append(String.format("variable \"%s\" {"
                    + "\n  type = string\n"
                    + "\n}\n", hclVar));
        }
        return hcl.toString();
    }

    public String getHclSecurityGroupRule() {
        if (ocl == null
                || ocl.getNetwork() == null
                || ocl.getNetwork().getSecurity() == null
                || ocl.getNetwork().getSecurity().get(0).getRules() == null) {
            throw new IllegalArgumentException("Ocl for security group rule is invalid.");
        }

        StringBuilder hcl = new StringBuilder();

        // todo: parse ports to port_range
        for (var secGroup : ocl.getNetwork().getSecurity()) {
            int index = 0;
            for (var securityRule : secGroup.getRules()) {
                var portPairs = getPortPairs(securityRule.getPorts());
                for (PortPair portPair : portPairs) {
                    hcl.append(String.format(
                            "\nresource \"huaweicloud_networking_secgroup_rule\"  \"%s_%d\" {"
                                    +
                                    "\n  security_group_id = huaweicloud_networking_secgroup.%s.id"
                                    + "\n  direction         = \"%s\""
                                    + "\n  ethertype         = \"IPV4\""
                                    + "\n  protocol          = \"%s\""
                                    + "\n  port_range_min    = \"%s\""
                                    + "\n  port_range_max    = \"%s\""
                                    + "\n  remote_ip_prefix  = \"%s\""
                                    + "\n}\n\n",
                            securityRule.getName(), index++, secGroup.getName(),
                            securityRule.getDirection().equals("inbound") ? "ingress" : "egress",
                            securityRule.getProtocol(), portPair.getFrom(), portPair.getTo(),
                            securityRule.getCidr()));
                }
            }
        }
        return hcl.toString();
    }

    public String getHclSecurityGroup() {
        if (ocl == null
                || ocl.getNetwork() == null
                || ocl.getNetwork().getSecurity() == null) {
            throw new IllegalArgumentException("Ocl for security group is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        for (var secGroup : ocl.getNetwork().getSecurity()) {
            hcl.append(String.format("\nresource \"huaweicloud_networking_secgroup\" \"%s\" {"
                            + "\n  name = \"%s\""
                            + "\n}\n\n",
                    secGroup.getName(), secGroup.getName()));
        }
        return hcl.toString();
    }

    public String getHclVpc() {
        if (ocl == null
                || ocl.getNetwork() == null
                || ocl.getNetwork().getVpc() == null) {
            throw new IllegalArgumentException("Ocl for VPC is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        for (var vpc : ocl.getNetwork().getVpc()) {
            hcl.append(String.format("\nresource \"huaweicloud_vpc\" \"%s\" {"
                            + "\n  name = \"%s\""
                            + "\n  cidr = \"%s\""
                            + "\n}\n\n",
                    vpc.getName(), vpc.getName(), vpc.getCidr()));
        }
        return hcl.toString();
    }

    public String getHclVpcSubnet() {
        if (ocl == null
                || ocl.getNetwork() == null
                || ocl.getNetwork().getSubnet() == null) {
            throw new IllegalArgumentException("Ocl for VPC subnet is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        InetAddressValidator validator = InetAddressValidator.getInstance();
        for (var subnet : ocl.getNetwork().getSubnet()) {
            hcl.append(String.format("\nresource \"huaweicloud_vpc_subnet\" \"%s\" {"
                            + "\n  name = \"%s\""
                            + "\n  cidr = \"%s\"",
                    subnet.getName(), subnet.getName(), subnet.getCidr()));

            String gateway = subnet.getCidr().replaceAll("\\.\\d*/.*", ".1");
            gateway = gateway.replaceAll(":\\d*/.*", ":1");
            if (validator.isValid(gateway)) {
                hcl.append("\n  gateway_ip = \"").append(gateway).append("\"");
            }

            Optional<Vpc> vpc = ocl.referTo(subnet.getVpc(), Vpc.class);
            vpc.ifPresent(value
                    -> hcl.append("\n  vpc_id = huaweicloud_vpc.")
                    .append(value.getName())
                    .append(".id"));
            hcl.append("\n}\n\n");
        }

        return hcl.toString();
    }

    public String getHclAvailabilityZone() {
        if (ocl == null) {
            throw new IllegalArgumentException("Ocl for AvailabilityZone is invalid.");
        }
        return "\ndata \"huaweicloud_availability_zones\" \"osc-az\" {}";
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
        if (ocl == null
                || ocl.getCompute() == null
                || ocl.getCompute().getVm() == null) {
            throw new IllegalArgumentException("Ocl for vm is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        for (var vm : ocl.getCompute().getVm()) {
            hcl.append(String.format("\nresource \"huaweicloud_compute_instance\" \"%s\" {"
                            + "\n  name = \"%s\"",
                    vm.getName(), vm.getName()));

            Optional<Artifact> artifact = ocl.referTo(vm.getImage(), Artifact.class);
            if (artifact.isPresent()) {
                hcl.append("\n  image_id = \"").append(artifact.get().getId()).append("\"");
            } else {
                log.error("image id not found.");
                hcl.append("\n  image_id = \"").append("image_id_not_found").append("\"");
            }
            hcl.append("\n  flavor_id = \"").append(vm.getType()).append("\"");

            for (var subnetPath : vm.getSubnet()) {
                Optional<Subnet> subnet = ocl.referTo(subnetPath, Subnet.class);
                subnet.ifPresent(value
                        -> hcl.append("\n  network {\n    uuid = huaweicloud_vpc_subnet.")
                        .append(value.getName())
                        .append(".id\n  }"));
            }

            hcl.append("\n  admin_pass = \"Cloud#1234\"");

            List<String> securityGroupList = new ArrayList<>();
            for (var secGroup : vm.getSecurity()) {
                Optional<Security> subnet = ocl.referTo(secGroup, Security.class);
                subnet.ifPresent(value -> securityGroupList.add(value.getName()));
            }
            String securityGroupids =
                    securityGroupList.stream()
                            .map(group -> "huaweicloud_networking_secgroup." + group + ".id")
                            .collect(Collectors.joining(","));
            hcl.append("\n  security_group_ids = [ ").append(securityGroupids).append(" ]");
            hcl.append("\n}\n\n");

            if (vm.isPublicly()) {
                hcl.append(String.format(""
                                + "resource \"huaweicloud_vpc_eip\" \"osc-eip-%s\" {\n"
                                + "  publicip {\n"
                                + "    type = \"5_bgp\"\n"
                                + "  }\n"
                                + "  bandwidth {\n"
                                + "    name        = \"osc-eip-%s\"\n"
                                + "    size        = 5\n"
                                + "    share_type  = \"PER\"\n"
                                + "    charge_mode = \"traffic\"\n"
                                + "  }\n"
                                + "}\n"
                                + "\n"
                                +
                                "resource "
                                + "\"huaweicloud_compute_eip_associate\"\"osc-eip-associated-%s\" "
                                + "{\n"
                                + "  public_ip   = huaweicloud_vpc_eip.osc-eip-%s.address\n"
                                + "  instance_id = huaweicloud_compute_instance.%s.id\n"
                                + "}", vm.getName(), vm.getName(), vm.getName(), vm.getName(),
                        vm.getName()));
            }
        }
        return hcl.toString();
    }

    public String getHclStorage() {
        if (ocl == null || ocl.getStorage() == null) {
            throw new IllegalArgumentException("Ocl for storage is invalid.");
        }

        StringBuilder hcl = new StringBuilder();
        for (var storage : ocl.getStorage()) {
            hcl.append(String.format("\nresource \"huaweicloud_evs_volume\" \"%s\" {"
                            + "\n  name = \"%s\""
                            + "\n  volume_type = \"%s\""
                            + "\n  size = \"%s\"",
                    storage.getName(), storage.getName(), storage.getType(),
                    storage.getSize().replaceAll("[^0-9]*GiB", "").strip()));
            // TODO: Add variable [var.availability_zone] for availability_zone
            hcl.append("\n  availability_zone = data.huaweicloud_availability_zones.osc-az"
                    + ".names[0]");
            hcl.append("\n}\n\n");
        }

        return hcl.toString();
    }

    public String getHcl() {
        return getHclVariables()
                + getHclSecurityGroup()
                + getHclSecurityGroupRule()
                + getHclVpc()
                + getHclVm()
                + getHclVpcSubnet()
                + getHclFlavor()
                + getHclImage()
                + getHclAvailabilityZone()
                + getHclStorage();
    }
}
