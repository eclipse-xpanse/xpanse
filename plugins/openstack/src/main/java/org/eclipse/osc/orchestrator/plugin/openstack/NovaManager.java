package org.eclipse.osc.orchestrator.plugin.openstack;

import org.eclipse.osc.modules.ocl.loader.Artifact;
import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import java.util.Collections;
import java.util.Optional;

public class NovaManager {

    public static void createVm(OSClient.OSClientV3 osClient, Artifact artifact, Ocl ocl) {
        osClient.compute().servers().boot(Builders
                .server()
                .name(artifact.getName())
                .flavor("3") // TODO To check how to get this value from OCL
                .image(GlanceManager.getImageId(osClient, artifact.getBase()))
                .networks(Collections.singletonList(NeutronManager.getVmNetworkId(
                        osClient, "external"))) // TODO To check how to get this value from OCL
                .userData(UserDataHelper.getUserData(artifact.getProvisioners(), ocl))
                .configDrive(true)
                .build());
    }

    public static String getVmConsoleLog(OSClient.OSClientV3 osClient, int numberOfLinesFromTheEnd, String vmName) {
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
}
