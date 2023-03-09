/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.DeployTask;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.eclipse.xpanse.modules.models.view.ServiceVo;
import org.eclipse.xpanse.orchestrator.register.RegisterServiceStorage;
import org.eclipse.xpanse.orchestrator.service.DeployServiceStorage;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
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


    private static final String TASK_ID = "TASK_ID";

    private final RegisterServiceStorage registerServiceStorage;

    private final DeployServiceStorage deployServiceStorage;

    @Getter
    private final List<Deployment> deployers = new ArrayList<>();

    @Getter
    private final List<OrchestratorPlugin> plugins = new ArrayList<>();

    @Autowired
    OrchestratorService(RegisterServiceStorage registerServiceStorage,
            DeployServiceStorage deployServiceStorage) {
        this.registerServiceStorage = registerServiceStorage;
        this.deployServiceStorage = deployServiceStorage;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext =
                    ((ContextRefreshedEvent) event).getApplicationContext();
            plugins.addAll(applicationContext.getBeansOfType(OrchestratorPlugin.class).values());
            if (plugins.isEmpty()) {
                log.warn("No xpanse plugins loaded by the runtime.");
            }

            deployers.addAll(applicationContext.getBeansOfType(Deployment.class).values());
            if (deployers.isEmpty()) {
                log.warn("No deployer loaded by the runtime.");
            }
        }
    }

    /**
     * Persist the result of the deployment.
     *
     * @param deployTask the task of the deployment.
     */
    public DeployServiceEntity getNewDeployServiceTask(DeployTask deployTask) {
        DeployServiceEntity entity = new DeployServiceEntity();
        entity.setId(deployTask.getId());
        entity.setCreateTime(new Date());
        entity.setVersion(StringUtils.lowerCase(deployTask.getCreateRequest().getVersion()));
        entity.setName(StringUtils.lowerCase(deployTask.getCreateRequest().getName()));
        entity.setCsp(deployTask.getCreateRequest().getCsp());
        entity.setCategory(deployTask.getCreateRequest().getCategory());
        entity.setFlavor(deployTask.getCreateRequest().getFlavor());
        entity.setCreateRequest(deployTask.getCreateRequest());
        return entity;
    }

    /**
     * Get deployment and fill deployTask for deploy service task.
     *
     * @param deployTask the task of deploy managed service name.
     */
    public Deployment getDeployHandler(DeployTask deployTask) {

        // Find the registered service and fill Ocl.
        RegisterServiceEntity serviceEntity = new RegisterServiceEntity();
        serviceEntity.setName(StringUtils.lowerCase(deployTask.getCreateRequest().getName()));
        serviceEntity.setVersion(StringUtils.lowerCase(deployTask.getCreateRequest().getVersion()));
        serviceEntity.setCsp(deployTask.getCreateRequest().getCsp());
        serviceEntity.setCategory(deployTask.getCreateRequest().getCategory());
        serviceEntity = registerServiceStorage.findRegisteredService(serviceEntity);
        if (Objects.isNull(serviceEntity) || Objects.isNull(serviceEntity.getOcl())) {
            throw new RuntimeException("Registered service not found");
        }
        // Set Ocl and CreateRequest
        deployTask.setOcl(serviceEntity.getOcl());
        deployTask.getCreateRequest().setOcl(serviceEntity.getOcl());
        // Check context validation
        checkContextValidation(deployTask);
        // Fill the handler
        fillHandler(deployTask);
        // get the deployment.
        return getDeployment(deployTask);
    }

    /**
     * Async method to deploy service.
     *
     * @param deployment deployment
     * @param deployTask deployTask
     */
    @Async("taskExecutor")
    @Transactional
    public void asyncDeployService(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployServiceEntity deployServiceEntity = getNewDeployServiceTask(deployTask);
        try {
            deployServiceEntity.setServiceState(ServiceState.DEPLOYING);
            deployServiceStorage.store(deployServiceEntity);
            DeployResult deployResult = deployment.deploy(deployTask);
            deployServiceEntity.setServiceState(ServiceState.DEPLOY_SUCCESS);
            deployServiceEntity.setDeployResult(deployResult);
            deployServiceStorage.store(deployServiceEntity);
        } catch (Exception e) {
            log.error("asyncDeployService failed.", e);
            deployServiceEntity.setServiceState(ServiceState.DEPLOY_FAILED);
            deployServiceStorage.store(deployServiceEntity);
        }

    }

    /**
     * Get deployment and fill deployTask for destroy service task.
     *
     * @param deployTask the task of deploy managed service name.
     */
    public Deployment getDestroyHandler(DeployTask deployTask) {
        // Find the deployed service.
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(deployTask.getId());
        if (Objects.isNull(deployServiceEntity) || Objects.isNull(
                deployServiceEntity.getCreateRequest())) {
            throw new RuntimeException(String.format("Deployed service with id %s not found",
                    deployTask.getId()));
        }
        // Get state of service.
        ServiceState state = deployServiceEntity.getServiceState();
        if (state.equals(ServiceState.DEPLOYING) || state.equals(ServiceState.DESTROYING)) {
            throw new RuntimeException(String.format("Service with id %s is %s.",
                    deployTask.getId(), state));
        }
        // Set Ocl and CreateRequest
        deployTask.setCreateRequest(deployServiceEntity.getCreateRequest());
        deployTask.setOcl(deployServiceEntity.getCreateRequest().getOcl());
        // Check context validation
        checkContextValidation(deployTask);
        // Fill the handler
        fillHandler(deployTask);
        // get the deployment.
        return getDeployment(deployTask);

    }

    /**
     * Async method to deploy service.
     *
     * @param deployment deployment
     * @param deployTask deployTask
     */
    @Async("taskExecutor")
    @Transactional
    public void asyncDestroyService(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(deployTask.getId());
        if (Objects.isNull(deployServiceEntity)) {
            throw new RuntimeException(String.format("Deployed service with id %s not found",
                    deployTask.getId()));
        }
        try {
            deployServiceEntity.setServiceState(ServiceState.DESTROYING);
            deployServiceStorage.store(deployServiceEntity);
            DeployResult deployResult = deployment.destroy(deployTask);
            deployServiceEntity.setServiceState(ServiceState.DESTROY_SUCCESS);
            deployServiceEntity.setDeployResult(deployResult);
            deployServiceStorage.store(deployServiceEntity);
        } catch (RuntimeException e) {
            log.error("asyncDestroyService failed", e);
            deployServiceEntity.setServiceState(ServiceState.DESTROY_FAILED);
            deployServiceStorage.store(deployServiceEntity);
        }

    }


    /**
     * List deploy services.
     *
     * @return serviceVos
     */
    public List<ServiceVo> listDeployServices() {
        List<DeployServiceEntity> deployServices =
                deployServiceStorage.services();
        return deployServices.stream().map(service -> {
            ServiceVo serviceVo = new ServiceVo();
            BeanUtils.copyProperties(service, serviceVo);
            return serviceVo;
        }).collect(Collectors.toList());

    }

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return deployService
     */
    public DeployServiceEntity getDeployServiceDetail(UUID id) {
        return deployServiceStorage.findDeployServiceById(id);

    }


    private void fillHandler(DeployTask deployTask) {
        // Find the deployment plugin and resource handler
        Optional<OrchestratorPlugin> pluginOptional =
                this.plugins.stream()
                        .filter(plugin -> plugin.getCsp() == deployTask.getCreateRequest().getCsp())
                        .findFirst();
        if (pluginOptional.isEmpty() || Objects.isNull(pluginOptional.get().getResourceHandler())) {
            throw new RuntimeException("Can't find suitable plugin and resource handler for the "
                    + "Task.");
        }
        deployTask.setDeployResourceHandler(pluginOptional.get().getResourceHandler());
    }

    private Deployment getDeployment(DeployTask deployTask) {
        DeployerKind deployerKind = deployTask.getOcl().getDeployment().getKind();
        Optional<Deployment> deploymentOptional =
                this.deployers.stream()
                        .filter(deployer -> deployer.getDeployerKind() == deployerKind)
                        .findFirst();
        if (deploymentOptional.isEmpty()) {
            throw new RuntimeException("Can't find suitable deployer for the Task.");
        }
        return deploymentOptional.get();
    }

    private void checkContextValidation(DeployTask deployTask) {
        // TODO check validation between ocl deployment context and createQuest property.
    }

}
