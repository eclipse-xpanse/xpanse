/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import java.util.Collections;
import java.util.Optional;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean to handle all requests to Nova APIs.
 */
@Component
public class NovaManager {

    private final GlanceManager glanceManager;
    private final NeutronManager neutronManager;

    @Autowired
    public NovaManager(GlanceManager glanceManager, NeutronManager neutronManager) {
        this.glanceManager = glanceManager;
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
     * @param artifact Artifact details of the virtual machine to be deployed.
     * @param ocl      Full OCL descriptor of the managed service to be deployed.
     */
    public void createVm(OSClient.OSClientV3 osClient, Artifact artifact, Ocl ocl) {
        osClient.compute().servers().boot(Builders
                .server()
                .name(artifact.getName())
                .flavor("3") // TODO To check how to get this value from OCL
                .image(this.glanceManager.getImageId(osClient, artifact.getBase()))
                .networks(Collections.singletonList(this.neutronManager.getVmNetworkId(
                        osClient, "external"))) // TODO To check how to get this value from OCL
                .userData(UserDataHelper.getUserData(artifact.getProvisioners(), ocl))
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
}
