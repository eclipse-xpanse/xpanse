/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.DeployTask;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.eclipse.xpanse.modules.models.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.view.ServiceVo;
import org.eclipse.xpanse.orchestrator.register.RegisterServiceStorage;
import org.eclipse.xpanse.orchestrator.service.DeployResourceStorage;
import org.eclipse.xpanse.orchestrator.service.DeployServiceStorage;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class OrchestratorService {

    private static final String TASK_ID = "TASK_ID";

    private final Map<Csp, OrchestratorPlugin> pluginMap = new ConcurrentHashMap<>();

    private final Map<DeployerKind, Deployment> deploymentMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private RegisterServiceStorage registerServiceStorage;
    @Resource
    private DeployServiceStorage deployServiceStorage;
    @Resource
    private DeployResourceStorage deployResourceStorage;
    @Resource
    private DeployVariableValidator deployVariableValidator;

    /**
     * Get all OrchestratorPlugin group by Csp.
     *
     * @return pluginMap
     */
    @Bean
    public Map<Csp, OrchestratorPlugin> pluginMap() {
        applicationContext.getBeansOfType(OrchestratorPlugin.class)
                .forEach((key, value) -> pluginMap.put(value.getCsp(), value));
        return pluginMap;
    }

    /**
     * Get all Deployment group by DeployerKind.
     *
     * @return deployerMap
     */
    @Bean
    public Map<DeployerKind, Deployment> deploymentMap() {
        applicationContext.getBeansOfType(Deployment.class)
                .forEach((key, value) -> deploymentMap.put(value.getDeployerKind(), value));
        return deploymentMap;
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
        // Check context validation
        if (Objects.nonNull(serviceEntity.getOcl().getDeployment()) && Objects.nonNull(
                deployTask.getCreateRequest().getProperty())) {
            List<DeployVariable> deployVariables = serviceEntity.getOcl().getDeployment()
                    .getContext();
            deployVariableValidator.isVariableValid(deployVariables,
                    deployTask.getCreateRequest().getProperty());
        }
        // Set Ocl and CreateRequest
        deployTask.setOcl(serviceEntity.getOcl());
        deployTask.getCreateRequest().setOcl(serviceEntity.getOcl());
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
            deployServiceEntity.setDeployResourceEntity(
                    getDeployResourceEntityList(deployResult.getResources(), deployServiceEntity));
            deployServiceStorage.store(deployServiceEntity);
        } catch (Exception e) {
            log.error("asyncDeployService failed.", e);
            deployServiceEntity.setServiceState(ServiceState.DEPLOY_FAILED);
            deployServiceStorage.store(deployServiceEntity);
        }

    }

    private List<DeployResourceEntity> getDeployResourceEntityList(
            List<DeployResource> deployResources, DeployServiceEntity deployServiceEntity) {
        List<DeployResourceEntity> deployResourceEntities = new ArrayList<>();
        if (CollectionUtils.isEmpty(deployResources)) {
            return deployResourceEntities;
        }
        for (DeployResource resource : deployResources) {
            DeployResourceEntity deployResource = new DeployResourceEntity();
            BeanUtils.copyProperties(resource, deployResource);
            deployResource.setDeployService(deployServiceEntity);
            deployResourceEntities.add(deployResource);
        }
        return deployResourceEntities;
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
            List<DeployResource> resources = deployResult.getResources();
            if (CollectionUtils.isEmpty(resources)) {
                deployResourceStorage.deleteByDeployServiceId(deployServiceEntity.getId());
            } else {
                deployServiceEntity.setDeployResourceEntity(
                        getDeployResourceEntityList(resources, deployServiceEntity));
            }
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
        OrchestratorPlugin plugin = pluginMap.get(deployTask.getCreateRequest().getCsp());
        if (Objects.isNull(plugin) || Objects.isNull(plugin.getResourceHandler())) {
            throw new RuntimeException("Can't find suitable plugin and resource handler for the "
                    + "Task.");
        }
        deployTask.setDeployResourceHandler(plugin.getResourceHandler());
    }

    private Deployment getDeployment(DeployTask deployTask) {
        DeployerKind deployerKind = deployTask.getOcl().getDeployment().getKind();
        Deployment deployment = deploymentMap.get(deployerKind);
        if (Objects.isNull(deployment)) {
            throw new RuntimeException("Can't find suitable deployer for the Task.");
        }
        return deployment;
    }


    /**
     * generate OpenApi for registered service using the ID.
     *
     * @param id ID of registered service.
     * @return path of openapi.html
     */
    public String getOpenApiUrl(String id) {
        UUID uuid = UUID.fromString(id);
        RegisterServiceEntity registerService = registerServiceStorage.getRegisterServiceById(uuid);
        if (Objects.isNull(registerService) || Objects.isNull(registerService.getOcl())) {
            throw new IllegalArgumentException(String.format("Registered service with id %s not "
                    + "existed.", id));
        }
        // TODO find the path of swagger-ui.html of the registered by id or generate swagger-ui.html
        return null;
    }


}
