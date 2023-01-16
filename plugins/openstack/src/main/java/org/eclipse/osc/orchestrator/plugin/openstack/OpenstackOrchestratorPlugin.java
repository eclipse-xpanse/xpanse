package org.eclipse.osc.orchestrator.plugin.openstack;

import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.magnum.Container;
import org.openstack4j.model.magnum.ContainerBuilder;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.openstack.magnum.MagnumContainer;

import java.util.Objects;

@Slf4j
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin, Service {

    private static final String SUCCESSFUL_INSTALLATION_LOG = "Kafka up and running"; //TODO - to be moved to Ocl?
    private OSClient.OSClientV3 osClient;

    @Override
    public String name() {
        return "osc-openstack-plugin";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        osClient = KeystoneManager.getClient(serviceRegistry);
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Register managed service, creating openstack resources");
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
                        NovaManager.createVm(this.osClient, artifact, ocl);
                        log.info("VM with name created {} and Kafka is being installed on it", artifact.getName());
                    } catch (Exception e) {
                        log.warn("Virtual machine {} create failed with exception ", artifact.getName(), e);
                    }
                    isKafkaProvisioningSuccessful(artifact.getName());
                }
            });
        }
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service {} on openstack", managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service {} on openstack", managedServiceName);
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service {} on openstack", managedServiceName);
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service {} from openstack", managedServiceName);
    }

    private boolean isKafkaProvisioningSuccessful(String vmName) {
        String vmConsoleLogs = NovaManager.getVmConsoleLog(this.osClient, 50, vmName);
        return vmConsoleLogs.contains(SUCCESSFUL_INSTALLATION_LOG);
    }
}
