/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class NovaManager {

    private final GlanceManager glanceManager;
    private final NeutronManager neutronManager;
    @Autowired
    public NovaManager(GlanceManager glanceManager, NeutronManager neutronManager) {
        this.glanceManager = glanceManager;
        this.neutronManager = neutronManager;
    }

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

    public String getVmConsoleLog(OSClient.OSClientV3 osClient, int numberOfLinesFromTheEnd, String vmName) {
        return osClient.compute().servers().getConsoleOutput(getVmId(osClient, vmName), numberOfLinesFromTheEnd);
    }

    public static String getVmId(OSClient.OSClientV3 osClient, String vmName) {
        Optional<? extends Server> server;
        server = osClient.compute().servers().list().stream().filter(serverInfo -> serverInfo.getName().equals(vmName)).findAny();
        if (server.isEmpty()) {
            throw new RuntimeException("No VM with name " + vmName + " found");
        }
        return server.get().getId();

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
