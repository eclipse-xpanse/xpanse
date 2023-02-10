/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Vm;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean to handle all requests to Nova APIs.
 */
@Component
public class NovaManager {

    private final NeutronManager neutronManager;

    @Autowired
    public NovaManager(NeutronManager neutronManager) {
        this.neutronManager = neutronManager;
    }

    /**
     * Method to get the VM ID based on the virtual machine name.
     *
     * @param osClient Fully initialized client to connect to an Openstack installation.
     * @param vmName   Name of the VM in query.
     * @return ID of the virtual machine. This is a unique value allocated by Openstack
     * for each VM created.
     */
    public static String getVmId(OSClient.OSClientV3 osClient, String vmName) {
        Optional<? extends Server> server;
        server = osClient.compute().servers().list().stream()
                .filter(serverInfo -> serverInfo.getName().equals(vmName)).findAny();
        if (server.isEmpty()) {
            throw new RuntimeException("No VM with name " + vmName + " found");
        }
        return server.get().getId();

    }

    /**
     * Method to create virtual machine to deploy a managed service.
     *
     * @param osClient Fully initialized client to connect to an Openstack installation.
     * @param vm       VM details of the virtual machine to be deployed.
     */
    public void createVm(OSClient.OSClientV3 osClient, Vm vm) {
        osClient.compute().servers().boot(Builders
                .server()
                .name(vm.getName())
                .flavor(getVmFlavourId(vm.getType(), osClient))
                .image(vm.getImageId())
                .networks(vm.getSubnets().stream()
                        .map(subnet -> this.neutronManager.getNetworkId(osClient, subnet)).collect(
                                Collectors.toList()))
                .userData(UserDataHelper.getUserData(vm.getUserData()))
                .configDrive(true)
                .build());
    }

    public String getVmConsoleLog(OSClient.OSClientV3 osClient, int numberOfLinesFromTheEnd,
                                  String vmName) {
        return osClient.compute().servers()
                .getConsoleOutput(getVmId(osClient, vmName), numberOfLinesFromTheEnd);
    }

    public void stopVm(String vmName, OSClient.OSClientV3 osClient) {
        String vmId = getVmId(osClient, vmName);
        osClient.compute().servers().action(vmId, Action.STOP);
    }

    public void deleteVm(String vmName, OSClient.OSClientV3 osClient) {
        String vmId = getVmId(osClient, vmName);
        osClient.compute().servers().action(vmId, Action.FORCEDELETE);
    }

    /**
     * Method to get ID of a VM flavour from an Openstack installation.
     *
     * @param flavourName name of the VM flavour.
     * @param osClient Fully initialized client to connect to an Openstack installation.
     * @return Unique ID of the VM flavour.
     */
    public String getVmFlavourId(String flavourName, OSClient.OSClientV3 osClient) {
        Optional<? extends Flavor> flavor;
        flavor = osClient.compute().flavors().list().stream()
                .filter(flavour -> flavour.getName().equals(flavourName)).findFirst();
        if (flavor.isEmpty()) {
            throw new RuntimeException("No flavour with name " + flavourName + " found");
        }
        return flavor.get().getId();
    }
}
