/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import jakarta.persistence.EntityNotFoundException;
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
    private final OrchestratorStorage orchestratorStorage;

    private final ApplicationContext applicationContext;

    /**
     * Constructor to instantiate Plugin bean.
     *
     * @param keystoneManager KeystoneManager bean.
     */
    @Autowired
    public OpenstackOrchestratorPlugin(KeystoneManager keystoneManager,
            OrchestratorStorage orchestratorStorage,
            ApplicationContext applicationContext) {
        log.info("Loading OpenstackOrchestratorPlugin");
        this.keystoneManager = keystoneManager;
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
    }
}
