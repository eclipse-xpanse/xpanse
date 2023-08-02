/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

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
import org.eclipse.xpanse.modules.database.register.RegisterServiceStorage;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.register.DeployVariable;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.service.register.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceNotRegisteredException;
import org.eclipse.xpanse.modules.models.service.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class DeployService {

    private static final String TASK_ID = "TASK_ID";

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
    @Resource
    private PluginManager pluginManager;
    @Resource
    private IdentityProviderManager identityProviderManager;
    @Resource
    private AesUtil aesUtil;

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
        entity.setName(StringUtils.lowerCase(deployTask.getCreateRequest().getServiceName()));
        entity.setCsp(deployTask.getCreateRequest().getCsp());
        entity.setCategory(deployTask.getCreateRequest().getCategory());
        entity.setCustomerServiceName(deployTask.getCreateRequest().getCustomerServiceName());
        entity.setFlavor(deployTask.getCreateRequest().getFlavor());
        entity.setUserId(deployTask.getCreateRequest().getUserId());
        entity.setCreateRequest(deployTask.getCreateRequest());
        entity.setDeployResourceList(new ArrayList<>());
        return entity;
    }

    /**
     * Get deployment and fill deployTask for deploy service task.
     *
     * @param deployTask the task of deploy managed service name.
     */
    public Deployment getDeployHandler(DeployTask deployTask) {

        deployTask.getCreateRequest().setUserId(getCurrentLoginUserId());

        // Find the registered service and fill Ocl.
        RegisterServiceEntity serviceEntity = new RegisterServiceEntity();
        serviceEntity.setName(
                StringUtils.lowerCase(deployTask.getCreateRequest().getServiceName()));
        serviceEntity.setVersion(StringUtils.lowerCase(deployTask.getCreateRequest().getVersion()));
        serviceEntity.setCsp(deployTask.getCreateRequest().getCsp());
        serviceEntity.setCategory(deployTask.getCreateRequest().getCategory());
        serviceEntity = registerServiceStorage.findRegisteredService(serviceEntity);
        if (Objects.isNull(serviceEntity) || Objects.isNull(serviceEntity.getOcl())) {
            throw new ServiceNotRegisteredException("Registered service not found");
        }
        // Check context validation
        if (Objects.nonNull(serviceEntity.getOcl().getDeployment()) && Objects.nonNull(
                deployTask.getCreateRequest().getServiceRequestProperties())) {
            List<DeployVariable> deployVariables = serviceEntity.getOcl().getDeployment()
                    .getVariables();
            deployVariableValidator.isVariableValid(deployVariables,
                    deployTask.getCreateRequest().getServiceRequestProperties());
        }
        encodeDeployVariable(serviceEntity,
                deployTask.getCreateRequest().getServiceRequestProperties());
        // Set Ocl and CreateRequest
        deployTask.setOcl(serviceEntity.getOcl());
        deployTask.getCreateRequest().setOcl(serviceEntity.getOcl());
        // Fill the handler
        fillHandler(deployTask);
        // get the deployment.
        return getDeployment(deployTask.getOcl().getDeployment().getKind());
    }

    private void encodeDeployVariable(RegisterServiceEntity serviceEntity,
                                      Map<String, String> serviceRequestProperties) {
        if (Objects.isNull(serviceEntity.getOcl().getDeployment())
                || CollectionUtils.isEmpty(serviceEntity.getOcl().getDeployment().getVariables())) {
            return;
        }
        serviceEntity.getOcl().getDeployment().getVariables().forEach(variable -> {
            if (Objects.nonNull(variable) && !SensitiveScope.NONE.toValue()
                    .equals(variable.getSensitiveScope().toValue())
                    && serviceRequestProperties.containsKey(variable.getName())) {
                serviceRequestProperties.put(variable.getName(),
                        aesUtil.encode(serviceRequestProperties.get(variable.getName())));
            }
        });
    }

    /**
     * Async method to deploy service.
     *
     * @param deployment deployment
     * @param deployTask deployTask
     */
    @Async("taskExecutor")
    public void asyncDeployService(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployServiceEntity deployServiceEntity = getNewDeployServiceTask(deployTask);
        try {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            DeployResult deployResult = deployment.deploy(deployTask);
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
            deployServiceEntity.setProperties(deployResult.getProperties());
            deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
            deployServiceEntity.setDeployResourceList(
                    getDeployResourceEntityList(deployResult.getResources(), deployServiceEntity));
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        } catch (RuntimeException e) {
            log.error("asyncDeployService failed.", e);
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_FAILED);
            deployServiceEntity.setResultMessage(e.getMessage());
            deployServiceStorage.storeAndFlush(deployServiceEntity);
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
            throw new ServiceNotDeployedException(
                    String.format("Deployed service with id %s not found",
                            deployTask.getId()));
        }
        if (!StringUtils.equals(getCurrentLoginUserId(), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No right to destroy resource or data belong to other users.");
        }
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (state.equals(ServiceDeploymentState.DEPLOYING)
                || state.equals(ServiceDeploymentState.DESTROYING)) {
            throw new InvalidServiceStateException(String.format("Service with id %s is %s.",
                    deployTask.getId(), state));
        }
        // Set Ocl and CreateRequest
        deployTask.setCreateRequest(deployServiceEntity.getCreateRequest());
        deployTask.setOcl(deployServiceEntity.getCreateRequest().getOcl());
        // Fill the handler
        fillHandler(deployTask);
        // get the deployment.
        return getDeployment(deployTask.getOcl().getDeployment().getKind());

    }

    /**
     * Async method to deploy service.
     *
     * @param deployment deployment
     * @param deployTask deployTask
     */
    @Async("taskExecutor")
    public void asyncDestroyService(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(deployTask.getId());
        if (Objects.isNull(deployServiceEntity)) {
            throw new ServiceNotDeployedException(
                    String.format("Deployed service with id %s not found",
                            deployTask.getId()));
        }
        try {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            DeployResult deployResult = deployment.destroy(deployTask,
                    deployServiceEntity.getPrivateProperties().get("stateFile"));
            if (deployResult.getState() == TerraformExecState.DESTROY_SUCCESS) {
                deployServiceEntity.setServiceDeploymentState(
                        ServiceDeploymentState.DESTROY_SUCCESS);
                deployServiceEntity.setProperties(deployResult.getProperties());
                deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
                List<DeployResource> resources = deployResult.getResources();
                if (CollectionUtils.isEmpty(resources)) {
                    deployResourceStorage.deleteByDeployServiceId(deployServiceEntity.getId());
                } else {
                    deployServiceEntity.setDeployResourceList(
                            getDeployResourceEntityList(resources, deployServiceEntity));
                }
                deployServiceStorage.storeAndFlush(deployServiceEntity);
            } else {
                deployServiceEntity.setServiceDeploymentState(
                        ServiceDeploymentState.DESTROY_FAILED);
                deployServiceStorage.storeAndFlush(deployServiceEntity);
            }
        } catch (Exception e) {
            log.error("asyncDestroyService failed", e);
            deployServiceEntity.setResultMessage(e.getMessage());
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROY_FAILED);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        }

    }

    /**
     * List deploy services for user.
     *
     * @return serviceVos
     */
    public List<ServiceVo> listMyDeployedServices() {
        List<DeployServiceEntity> deployServices =
                deployServiceStorage.services();
        String currentUserId = getCurrentLoginUserId();
        return deployServices.stream()
                .filter(service -> StringUtils.equals(currentUserId, service.getUserId()))
                .map(service -> {
                    ServiceVo serviceVo = new ServiceVo();
                    BeanUtils.copyProperties(service, serviceVo);
                    return serviceVo;
                }).collect(Collectors.toList());

    }

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return serviceDetailVo
     */
    public ServiceDetailVo getDeployServiceDetails(UUID id) {
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(deployServiceEntity)) {
            throw new ServiceNotDeployedException("Service not found.");
        }

        if (!StringUtils.equals(getCurrentLoginUserId(), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No right to view details of service belong to other users.");
        }

        ServiceDetailVo serviceDetailVo = new ServiceDetailVo();
        BeanUtils.copyProperties(deployServiceEntity, serviceDetailVo);
        if (!CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
            List<DeployResource> deployResources =
                    EntityTransUtils.transResourceEntity(
                            deployServiceEntity.getDeployResourceList());
            serviceDetailVo.setDeployResources(deployResources);
        }
        if (!CollectionUtils.isEmpty(deployServiceEntity.getProperties())) {
            serviceDetailVo.setDeployedServiceProperties(deployServiceEntity.getProperties());
        }
        return serviceDetailVo;
    }


    private void fillHandler(DeployTask deployTask) {
        // Find the deployment plugin and resource handler
        OrchestratorPlugin plugin =
                pluginManager.getPluginsMap().get(deployTask.getCreateRequest().getCsp());
        if (Objects.isNull(plugin) || Objects.isNull(plugin.getResourceHandler())) {
            throw new PluginNotFoundException(
                    "Can't find suitable plugin and resource handler for the "
                            + "Task.");
        }
        deployTask.setDeployResourceHandler(plugin.getResourceHandler());
    }

    /**
     * Get Deployment bean available for the requested DeployerKind.
     *
     * @param deployerKind Deployer for which the Deployment bean is required.
     * @return Deployment bean for the provided deployerKind.
     */
    public Deployment getDeployment(DeployerKind deployerKind) {
        Deployment deployment = deploymentMap.get(deployerKind);
        if (Objects.isNull(deployment)) {
            throw new DeployerNotFoundException("Can't find suitable deployer for the Task.");
        }
        return deployment;
    }

    private String getCurrentLoginUserId() {
        CurrentUserInfo currentUserInfo = identityProviderManager.getCurrentUserInfo();
        if (Objects.nonNull(currentUserInfo)
                && StringUtils.isNotBlank(currentUserInfo.getUserId())) {
            return currentUserInfo.getUserId();
        } else {
            return "defaultUserId";
        }
    }

}
