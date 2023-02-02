/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import java.util.Optional;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Network;
import org.springframework.stereotype.Component;

/**
 * Bean to handle all requests to Neutron APIs.
 */
@Component
public class NeutronManager {

    /**
     * Creates the network required to deploy a managed service.
     *
     * @param ocl Ocl request object to deploy a managed service.
     * @param os  fully authenticated and instantiated Openstack client object.
     */
    public void createNetwork(Ocl ocl, OSClient.OSClientV3 os) {
        ocl.getNetwork().getSubnet()
                .forEach(subnet -> os.networking().subnet().create(Builders.subnet()
                        .name(subnet.getId())
                        .ipVersion(IPVersionType.V4)
                        .cidr(subnet.getCidr())
                        .build()));
    }

    /**
     * Method to get the image ID based on the network name.
     *
     * @param osClient      Fully initialized client to connect to an Openstack installation.
     * @param vmNetworkName Name of the virtual network in query.
     * @return ID of the virtual network. This is a unique value allocated by Openstack
     * for each network created.
     */
    public String getVmNetworkId(OSClient.OSClientV3 osClient, String vmNetworkName) {
        Optional<? extends Network> network;
        network = osClient.networking()
                .network()
                .list()
                .stream()
                .filter(networkInfo -> networkInfo.getName().equalsIgnoreCase(vmNetworkName))
                .findAny();
        if (network.isEmpty()) {
            throw new RuntimeException("No network with name " + network + " found");
        }
        return network.get().getId();
    }

}
