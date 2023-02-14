/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorStorage;
import org.openstack4j.api.OSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * xpanse plugin implementation for openstack cloud.
 */
@Slf4j
@Component
@Profile(value = "openstack")
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin {

    private final KeystoneManager keystoneManager;
    private final NovaManager novaManager;
    private final NeutronManager neutronManager;
    private final OrchestratorStorage orchestratorStorage;

    private final ApplicationContext applicationContext;

    /**
     * Constructor to instantiate Plugin bean.
     *
     * @param keystoneManager KeystoneManager bean.
     * @param novaManager     NovaManager bean.
     */
    @Autowired
    public OpenstackOrchestratorPlugin(KeystoneManager keystoneManager, NovaManager novaManager,
                                       NeutronManager neutronManager,
                                       OrchestratorStorage orchestratorStorage,
                                       ApplicationContext applicationContext) {
        log.info("Loading OpenstackOrchestratorPlugin");
        this.keystoneManager = keystoneManager;
        this.novaManager = novaManager;
        this.neutronManager = neutronManager;
        this.orchestratorStorage = orchestratorStorage;
        this.applicationContext = applicationContext;
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Registering service " + ocl.getName() + "for openstack plugin");
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service {} on openstack", managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        if (!this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                managedServiceName,
                this.applicationContext.getBean(OpenstackOrchestratorPlugin.class))) {
            throw new EntityNotFoundException(
                    "Service with name " + managedServiceName + " is not registered.");
        }
        log.info("Start managed service {} on openstack", managedServiceName);
        OSClient.OSClientV3 osClient = this.keystoneManager.getClient();
        Ocl ocl = this.orchestratorStorage.getServiceDetailsByNameAndPlugin(
                managedServiceName,
                this.applicationContext.getBean(OpenstackOrchestratorPlugin.class)).getOcl();

        if (Objects.nonNull(ocl.getNetwork())) {
            log.info("Creating network resources via neutron API.");
            ocl.getNetwork().getSubnets()
                    .forEach(subnet -> this.neutronManager.createNetwork(subnet, osClient));
        }
        if (Objects.nonNull(ocl.getCompute())) {
            ocl.getCompute().getVms().forEach(vm -> {
                log.info("Starting bare VM via Nova");
                try {
                    this.novaManager.createVm(osClient, vm);
                    log.info("VM with name created {} and Kafka is being installed on it",
                            vm.getName());
                } catch (Exception e) {
                    log.warn("Virtual machine {} create failed with exception ",
                            vm.getName(), e);
                }
            });
        }
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        if (!this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                managedServiceName,
                this.applicationContext.getBean(OpenstackOrchestratorPlugin.class))) {
            throw new EntityNotFoundException(
                    "Service with name " + managedServiceName + " is not registered.");
        }
        log.info("Stop managed service {} on openstack", managedServiceName);
        OSClient.OSClientV3 osClient = this.keystoneManager.getClient();
        Ocl ocl = this.orchestratorStorage.getServiceDetailsByNameAndPlugin(
                managedServiceName,
                this.applicationContext.getBean(OpenstackOrchestratorPlugin.class)).getOcl();
        if (Objects.nonNull(ocl.getCompute())) {
            ocl.getCompute().getVms().forEach(vm -> {
                log.info("Stopping bare VM via Nova API");
                this.novaManager.stopVm(vm.getName(), osClient);
            });
        }
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        if (!this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                managedServiceName,
                this.applicationContext.getBean(OpenstackOrchestratorPlugin.class))) {
            throw new EntityNotFoundException(
                    "Service with name " + managedServiceName + " is not registered.");
        }
        log.info("Destroy managed service {} from openstack", managedServiceName);
        OSClient.OSClientV3 osClient = this.keystoneManager.getClient();
        Ocl ocl = this.orchestratorStorage.getServiceDetailsByNameAndPlugin(
                managedServiceName,
                this.applicationContext.getBean(OpenstackOrchestratorPlugin.class)).getOcl();
        if (Objects.nonNull(ocl.getCompute())) {
            ocl.getCompute().getVms().forEach(vm -> {
                log.info("Deleting bare VM via Nova API");
                this.novaManager.deleteVm(vm.getName(), osClient);
            });
        }
    }
}
