/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.openstack4j.api.OSClient;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final Map<String, Ocl> managedOcl = new HashMap<>();

    /**
     * Constructor to instantiate Plugin bean.
     *
     * @param keystoneManager KeystoneManager bean.
     * @param novaManager     NovaManager bean.
     */
    @Autowired
    public OpenstackOrchestratorPlugin(KeystoneManager keystoneManager, NovaManager novaManager,
            NeutronManager neutronManager) {
        log.info("Loading OpenstackOrchestratorPlugin");
        this.keystoneManager = keystoneManager;
        this.novaManager = novaManager;
        this.neutronManager = neutronManager;
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        managedOcl.put(ocl.getName(), ocl);
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service {} on openstack", managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service {} on openstack", managedServiceName);
        OSClient.OSClientV3 osClient = this.keystoneManager.getClient();
        Ocl ocl = managedOcl.get(managedServiceName);
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
        log.info("Stop managed service {} on openstack", managedServiceName);
        OSClient.OSClientV3 osClient = this.keystoneManager.getClient();
        Ocl ocl = managedOcl.get(managedServiceName);
        if (Objects.nonNull(ocl.getCompute())) {
            ocl.getCompute().getVms().forEach(vm -> {
                log.info("Stopping bare VM via Nova API");
                this.novaManager.stopVm(vm.getName(), osClient);
            });
        }
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service {} from openstack", managedServiceName);
        OSClient.OSClientV3 osClient = this.keystoneManager.getClient();
        Ocl ocl = managedOcl.get(managedServiceName);
        if (Objects.nonNull(ocl.getCompute())) {
            ocl.getCompute().getVms().forEach(vm -> {
                log.info("Deleting bare VM via Nova API");
                this.novaManager.deleteVm(vm.getName(), osClient);
            });
        }
    }
}
