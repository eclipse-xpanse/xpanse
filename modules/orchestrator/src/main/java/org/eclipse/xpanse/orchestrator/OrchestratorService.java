/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployResult;
import org.eclipse.xpanse.modules.deployment.DeployTask;
import org.eclipse.xpanse.modules.ocl.loader.data.models.CloudServiceProvider;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.TaskType;
import org.eclipse.xpanse.modules.service.CreateRequest;
import org.eclipse.xpanse.modules.terraform.deployment.TerraformDeployment;
import org.eclipse.xpanse.service.RegisterServiceStorage;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Transactional
@Component
public class OrchestratorService implements ApplicationListener<ApplicationEvent> {

    private static final String TASK_TYPE = "TASK_TYPE";

    private final RegisterServiceStorage registerServiceStorage;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private List<OrchestratorPlugin> pluginList;

    @Getter
    private final List<OrchestratorPlugin> plugins = new ArrayList<>();

    @Autowired
    OrchestratorService(RegisterServiceStorage registerServiceStorage) {
        this.registerServiceStorage = registerServiceStorage;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext =
                    ((ContextRefreshedEvent) event).getApplicationContext();
            plugins.addAll(applicationContext.getBeansOfType(OrchestratorPlugin.class).values());
            if (plugins.size() > 1) {
                throw new RuntimeException("More than one xpanse plugin found. "
                        + "Only one plugin can be active at a time.");
            }
            if (plugins.isEmpty()) {
                log.warn("No xpanse plugins loaded by the runtime.");
            }
        }
    }

    /**
     * Persist the result of the deployment.
     *
     * @param deployResult the result of the deployment.
     */
    public void storeDeployService(DeployResult deployResult) {
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setVersion(deployResult.getTask().getCreateRequest().getVersion());
        deployServiceEntity.setName(deployResult.getTask().getCreateRequest().getName());
        deployServiceEntity.setId(deployResult.getTask().getCreateRequest().getId());
        deployServiceEntity.setCsp(deployResult.getTask().getCreateRequest().getCsp());
        deployServiceEntity.setFlavor(deployResult.getTask().getCreateRequest().getFlavor());
        deployServiceEntity.setProperty(deployResult.getTask().getCreateRequest().getProperty());
        deployServiceEntity.setOcl(deployResult.getTask().getOcl());
        deployServiceEntity.setDeployResourceEntity(deployResult.getResources());
        this.entityManager.persist(deployServiceEntity);
    }

    /**
     * Start (expose to users) a managed service on all orchestrator plugins.
     *
     * @param deployRequest the managed service name.
     */
    @Async("taskExecutor")
    public void startManagedService(CreateRequest deployRequest) {
        MDC.put(TASK_TYPE, TaskType.START.toValue());

        // todo: refactor this part with a new method.
        Ocl ocl = new Ocl();
        ocl.setName(deployRequest.getName());
        ocl.setServiceVersion(deployRequest.getVersion());

        CloudServiceProvider provider = new CloudServiceProvider();
        provider.setName(deployRequest.getCsp());
        RegisterServiceEntity serviceEntity = new RegisterServiceEntity();

        ocl.setCloudServiceProvider(provider);
        serviceEntity.setOcl(ocl);
        serviceEntity = this.registerServiceStorage.getRegisterService(serviceEntity);
        ocl = serviceEntity.getOcl();

        // Deploy the Task with deployment.
        DeployTask deployTask = new DeployTask();
        deployTask.setOcl(ocl);
        deployTask.setId(deployRequest.getId());
        deployTask.setCreateRequest(deployRequest);
        List<OrchestratorPlugin> plugins =
                pluginList.stream()
                        .filter(plugin -> plugin.getCsp() == deployRequest.getCsp())
                        .collect(Collectors.toList());
        if (plugins.size() != 1) {
            throw new RuntimeException("Can't find suitable plugin for the Task.");
        }
        deployTask.setDeployResourceHandler(plugins.get(0).getResourceHandler());

        TerraformDeployment terraformDeployment = new TerraformDeployment();
        DeployResult deployResult = terraformDeployment.deploy(deployTask);

        // Persist the deployservice.
        storeDeployService(deployResult);
    }

    /**
     * Stop (managed service is not visible to users anymore) a managed service on all orchestrator
     * plugins.
     *
     * @param id the id of the managed service.
     */
    @Async("taskExecutor")
    public void stopManagedService(String id) {
        MDC.put(TASK_TYPE, TaskType.STOP.toValue());
        log.info("Stop managedService : id = {}", id);
    }
}
