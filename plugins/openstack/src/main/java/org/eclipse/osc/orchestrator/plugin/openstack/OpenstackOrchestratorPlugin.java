package org.eclipse.osc.orchestrator.plugin.openstack;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.magnum.Container;
import org.openstack4j.model.magnum.ContainerBuilder;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.openstack.magnum.MagnumContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@Profile(value = "openstack")
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin {

    private static final String SUCCESSFUL_INSTALLATION_LOG = "Kafka up and running"; //TODO - to be moved to Ocl?
    private final OSClient.OSClientV3 osClient;
    private final NovaManager novaManager;

    private final Map<String, Ocl> managedOcl = new HashMap<>();

    @Autowired
    public OpenstackOrchestratorPlugin(KeystoneManager keystoneManager, NovaManager novaManager) {
        this.osClient = keystoneManager.getClient();
        this.novaManager = novaManager;
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Register managed service, creating openstack resources");
        managedOcl.put(ocl.getName(), ocl);
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service {} on openstack", managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service {} on openstack", managedServiceName);
        log.info("Register managed service, creating openstack resources");
        Ocl ocl = managedOcl.get(managedServiceName);
        if (Objects.nonNull(ocl.getNetwork())) {
            log.info("Creating Neutron network resources ...");
            ocl.getNetwork().getSubnet().forEach(subnet -> osClient.networking().subnet().create(Builders.subnet()
                    .name(subnet.getId())
                    .ipVersion(IPVersionType.V4)
                    .cidr(subnet.getCidr())
                    .build()));
        }
        if (Objects.nonNull(ocl.getImage())) {
            ocl.getImage().getArtifacts().forEach(artifact -> {
                if (artifact.getType().equalsIgnoreCase("docker")) {
                    log.info("Starting docker container via Magnum ...");
                    ContainerBuilder builder = new MagnumContainer.ContainerConcreteBuilder();
                    builder.image(artifact.getBase());
                    builder.name(artifact.getName());
                    Container container = osClient.magnum().createContainer(builder.build());
                    osClient.magnum().startContainer(artifact.getName());
                    log.info("Docker container " + container.getStatus());
                }
                if (artifact.getType().equalsIgnoreCase("image")) {
                    log.info("Starting bare VM via Nova ...");
                    try {
                        this.novaManager.createVm(this.osClient, artifact, ocl);
                        log.info("VM with name created {} and Kafka is being installed on it", artifact.getName());
                    } catch (Exception e) {
                        log.warn("Virtual machine {} create failed with exception ", artifact.getName(), e);
                    }
                    isProvisioningSuccessful(artifact.getName());
                }
            });
        }
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service {} on openstack", managedServiceName);
        Ocl ocl = managedOcl.get(managedServiceName);
        if (Objects.nonNull(ocl.getImage())) {
            ocl.getImage().getArtifacts().forEach(artifact -> {
                if (artifact.getType().equalsIgnoreCase("image")) {
                    log.info("Stopping bare VM via Nova ...");
                    this.novaManager.stopVm(artifact.getName(), this.osClient);
                }
            });
        }
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service {} from openstack", managedServiceName);
        Ocl ocl = managedOcl.get(managedServiceName);
        if (Objects.nonNull(ocl.getImage())) {
            ocl.getImage().getArtifacts().forEach(artifact -> {
                if (artifact.getType().equalsIgnoreCase("image")) {
                    log.info("Deleting bare VM via Nova ...");
                    this.novaManager.deleteVm(artifact.getName(), this.osClient);
                }
            });
        }
    }

    @VisibleForTesting
    boolean isProvisioningSuccessful(String vmName) {
        String vmConsoleLogs = this.novaManager.getVmConsoleLog(this.osClient, 50, vmName);
        return vmConsoleLogs.contains(SUCCESSFUL_INSTALLATION_LOG);
    }
}
