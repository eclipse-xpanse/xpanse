/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.ServiceStatusEntity;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.RuntimeState;
import org.eclipse.xpanse.modules.ocl.state.OclResources;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on Huawei cloud.
 */
@Slf4j
@Component
@Profile(value = "huaweicloud")
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin {

    private final Environment environment;

    private final OrchestratorStorage orchestratorStorage;

    private final ApplicationContext context;

    /**
     * Default constructor for the HuaweiCloudOrchestratorPlugin bean.
     *
     * @param environment Environment bean from Spring framework
     * @param orchestratorStorage Orchestrator storage bean
     * @param context Application context of the Spring framework
     */
    @Autowired
    public HuaweiCloudOrchestratorPlugin(Environment environment,
                                         OrchestratorStorage orchestratorStorage,
                                         ApplicationContext context) {
        this.environment = environment;
        this.orchestratorStorage = orchestratorStorage;
        this.context = context;
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Register managed service for HuaweiCloud");
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        if (!this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                managedServiceName, this.context.getBean(HuaweiCloudOrchestratorPlugin.class))) {
            throw new IllegalArgumentException(
                    "Service with name " + managedServiceName + " is not registered.");
        }
        log.info("Updating managed service {} on Huawei Cloud", managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service {} on Huawei Cloud", managedServiceName);
        if (!this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                managedServiceName, this.context.getBean(HuaweiCloudOrchestratorPlugin.class))) {
            throw new EntityNotFoundException(
                    "Service with name " + managedServiceName + " is not registered.");
        }

        Ocl ocl = getOcl(managedServiceName);
        BuilderFactory factory = new BuilderFactory();
        AtomBuilder envBuilder = factory.createBuilder(BuilderFactory.ENV_BUILDER, ocl);
        AtomBuilder basicBuilder = factory.createBuilder(BuilderFactory.BASIC_BUILDER, ocl);

        BuilderContext ctx = new BuilderContext();
        ctx.setEnvironment(this.environment);

        OclResources oclResources = getOclResources(managedServiceName);
        if (oclResources != null && oclResources.getState() == RuntimeState.ACTIVE) {
            log.info("Managed service {} already in active.", managedServiceName);
            return;
        }

        ctx.getOclResources().setState(RuntimeState.BUILDING);
        storeOclResources(managedServiceName, ctx.getOclResources());

        try {
            envBuilder.build(ctx);
            basicBuilder.build(ctx);
        } catch (Exception ex) {
            envBuilder.build(ctx);
            basicBuilder.rollback(ctx);
            throw ex;
        }
        ctx.getOclResources().setState(RuntimeState.ACTIVE);
        storeOclResources(managedServiceName, ctx.getOclResources());
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service {} on Huawei Cloud", managedServiceName);
        if (!this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                managedServiceName, this.context.getBean(HuaweiCloudOrchestratorPlugin.class))) {
            throw new EntityNotFoundException(
                    "Service with name " + managedServiceName + " is not registered.");
        }

        Ocl ocl = getOcl(managedServiceName);
        BuilderFactory factory = new BuilderFactory();
        AtomBuilder envBuilder = factory.createBuilder(BuilderFactory.ENV_BUILDER, ocl);
        AtomBuilder basicBuilder = factory.createBuilder(BuilderFactory.BASIC_BUILDER, ocl);

        BuilderContext ctx = new BuilderContext();
        ctx.setEnvironment(this.environment);

        envBuilder.build(ctx);
        basicBuilder.rollback(ctx);

        storeOclResources(managedServiceName, new OclResources());
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service {} from Huawei Cloud", managedServiceName);
        if (!this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                managedServiceName, this.context.getBean(HuaweiCloudOrchestratorPlugin.class))) {
            throw new EntityNotFoundException(
                    "Service with name " + managedServiceName + " is not registered.");
        }
    }

    private Ocl getOcl(String managedServiceName) {
        return this.orchestratorStorage.getServiceDetailsByNameAndPlugin(
                        managedServiceName,
                        this.context.getBean(HuaweiCloudOrchestratorPlugin.class))
                .getOcl().deepCopy();
    }

    private void storeOclResources(String managedServiceName, OclResources oclResources) {
        ServiceStatusEntity serviceStatusEntity = this.orchestratorStorage.getServiceDetailsByName(
                managedServiceName);
        serviceStatusEntity.setResources(oclResources);
        this.orchestratorStorage.store(serviceStatusEntity);
    }

    private OclResources getOclResources(String managedServiceName) {
        return this.orchestratorStorage.getServiceDetailsByName(
                managedServiceName).getResources();
    }
}
