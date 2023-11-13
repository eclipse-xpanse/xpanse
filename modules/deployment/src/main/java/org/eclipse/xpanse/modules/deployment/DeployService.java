/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformResult;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.query.ServiceQueryModel;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
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

    public static final String STATE_FILE_NAME = "terraform.tfstate";

    private final Map<DeployerKind, Deployment> deploymentMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private DeployServiceStorage deployServiceStorage;
    @Resource
    private DeployResourceStorage deployResourceStorage;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private IdentityProviderManager identityProviderManager;
    @Resource
    private AesUtil aesUtil;
    @Resource
    private ServiceVariablesJsonSchemaValidator serviceVariablesJsonSchemaValidator;

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
        entity.setCreateTime(OffsetDateTime.now());
        entity.setVersion(StringUtils.lowerCase(deployTask.getDeployRequest().getVersion()));
        entity.setName(StringUtils.lowerCase(deployTask.getDeployRequest().getServiceName()));
        entity.setCsp(deployTask.getDeployRequest().getCsp());
        entity.setCategory(deployTask.getDeployRequest().getCategory());
        entity.setCustomerServiceName(deployTask.getDeployRequest().getCustomerServiceName());
        entity.setFlavor(deployTask.getDeployRequest().getFlavor());
        entity.setUserId(deployTask.getDeployRequest().getUserId());
        entity.setDeployRequest(deployTask.getDeployRequest());
        entity.setDeployResourceList(new ArrayList<>());
        return entity;
    }

    /**
     * Get deployment and fill deployTask for deploy service task.
     *
     * @param deployTask the task of deploy managed service name.
     */
    public Deployment getDeployHandler(DeployTask deployTask) {
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        deployTask.getDeployRequest().setUserId(userIdOptional.orElse(null));

        // Find the registered service template and fill Ocl.
        ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setName(
                StringUtils.lowerCase(deployTask.getDeployRequest().getServiceName()));
        serviceTemplate.setVersion(
                StringUtils.lowerCase(deployTask.getDeployRequest().getVersion()));
        serviceTemplate.setCsp(deployTask.getDeployRequest().getCsp());
        serviceTemplate.setCategory(deployTask.getDeployRequest().getCategory());
        serviceTemplate.setServiceHostingType(
                deployTask.getDeployRequest().getServiceHostingType());
        serviceTemplate = serviceTemplateStorage.findServiceTemplate(serviceTemplate);
        if (Objects.isNull(serviceTemplate) || Objects.isNull(serviceTemplate.getOcl())) {
            throw new ServiceTemplateNotRegistered("Service template not found.");
        }
        // Check context validation
        if (Objects.nonNull(serviceTemplate.getOcl().getDeployment()) && Objects.nonNull(
                deployTask.getDeployRequest().getServiceRequestProperties())) {
            List<DeployVariable> deployVariables = serviceTemplate.getOcl().getDeployment()
                    .getVariables();

            serviceVariablesJsonSchemaValidator.validateDeployVariables(deployVariables,
                    deployTask.getDeployRequest().getServiceRequestProperties(),
                    serviceTemplate.getJsonObjectSchema());
        }
        encodeDeployVariable(serviceTemplate,
                deployTask.getDeployRequest().getServiceRequestProperties());
        // Set Ocl and CreateRequest
        deployTask.setOcl(serviceTemplate.getOcl());
        deployTask.getDeployRequest().setOcl(serviceTemplate.getOcl());
        // Fill the handler
        fillHandler(deployTask);
        // get the deployment.
        return getDeployment(deployTask.getOcl().getDeployment().getKind());
    }

    private void encodeDeployVariable(ServiceTemplateEntity serviceTemplate,
                                      Map<String, Object> serviceRequestProperties) {
        if (Objects.isNull(serviceTemplate.getOcl().getDeployment())
                ||
                CollectionUtils.isEmpty(serviceTemplate.getOcl().getDeployment().getVariables())
                || Objects.isNull(serviceRequestProperties)) {
            return;
        }
        serviceTemplate.getOcl().getDeployment().getVariables().forEach(variable -> {
            if (Objects.nonNull(variable) && !SensitiveScope.NONE.toValue()
                    .equals(variable.getSensitiveScope().toValue())
                    && serviceRequestProperties.containsKey(variable.getName())) {
                serviceRequestProperties.put(variable.getName(),
                        aesUtil.encode(
                                serviceRequestProperties.get(variable.getName()).toString()));
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
        deploy(deployment, deployTask);
    }

    private void deploy(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployServiceEntity deployServiceEntity = getNewDeployServiceTask(deployTask);
        DeployResult deployResult = new DeployResult();
        try {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            deployment.deploy(deployTask);
        } catch (RuntimeException e) {
            log.error("asyncDeployService failed.", e);
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_FAILED);
            deployServiceEntity.setResultMessage(e.getMessage());
            deployServiceEntity.setProperties(deployResult.getProperties());
            deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
            deployServiceEntity.setDeployResourceList(
                    getDeployResourceEntityList(deployResult.getResources(), deployServiceEntity));
            maskSensitiveFields(deployServiceEntity);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            rollbackOnDeploymentFailure(deployServiceEntity, deployResult, deployment, deployTask);
        }
    }

    /**
     * Perform rollback when deployment fails and destroy the created resources.
     */
    private void rollbackOnDeploymentFailure(DeployServiceEntity deployServiceEntity,
                                             DeployResult deployResult, Deployment deployment,
                                             DeployTask deployTask) {
        if (!CollectionUtils.isEmpty(deployResult.getResources())) {
            log.info("Performing rollback of already provisioned resources.");
            String stateFile = deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
            DeployResult destroyResult = deployment.destroy(deployTask, stateFile);
            if (destroyResult.getState() == TerraformExecState.DESTROY_SUCCESS) {
                deployServiceEntity.setProperties(destroyResult.getProperties());
                deployServiceEntity.setPrivateProperties(destroyResult.getPrivateProperties());
                deployServiceEntity.setDeployResourceList(
                        getDeployResourceEntityList(destroyResult.getResources(),
                                deployServiceEntity));
            } else {
                deployServiceEntity.setServiceDeploymentState(
                        ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED);
            }
            if (deployServiceStorage.storeAndFlush(deployServiceEntity)) {
                deployment.deleteTaskWorkspace(deployTask.getId().toString());
            }
        } else {
            log.info("No resources to rollback.");
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
        if (Objects.isNull(deployServiceEntity)
                || Objects.isNull(deployServiceEntity.getDeployRequest())) {
            String errorMsg = String.format("Service with id %s not found.", deployTask.getId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to destroy services belonging to other users.");
        }
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (state.equals(ServiceDeploymentState.DEPLOYING)
                || state.equals(ServiceDeploymentState.DESTROYING)) {
            throw new InvalidServiceStateException(String.format("Service with id %s is %s.",
                    deployTask.getId(), state));
        }
        // Set Ocl and CreateRequest
        deployTask.setDeployRequest(deployServiceEntity.getDeployRequest());
        deployTask.setOcl(deployServiceEntity.getDeployRequest().getOcl());
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
        destroy(deployment, deployTask);
    }

    private void destroy(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(deployTask.getId());
        if (Objects.isNull(deployServiceEntity)) {
            String errorMsg = String.format("Service with id %s not found.", deployTask.getId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException("Service with id %s not found.");
        }
        try {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            String stateFile = deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
            deployment.destroy(deployTask, stateFile);
        } catch (Exception e) {
            log.error("asyncDestroyService failed", e);
            deployServiceEntity.setResultMessage(e.getMessage());
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROY_FAILED);
            if (deployServiceStorage.storeAndFlush(deployServiceEntity)) {
                deployment.deleteTaskWorkspace(deployTask.getId().toString());
            }
        }

    }

    /**
     * purge the service based on the serviceDeploymentState.
     *
     * @param deployment deployment
     * @param deployTask deployTask
     */
    public void purgeService(Deployment deployment, DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(deployTask.getId());
        ServiceDeploymentState serviceDeploymentState =
                deployServiceEntity.getServiceDeploymentState();
        if (serviceDeploymentState == ServiceDeploymentState.DEPLOY_FAILED
                || serviceDeploymentState == ServiceDeploymentState.DESTROY_SUCCESS
                || serviceDeploymentState == ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED) {
            asyncPurgeService(deployment, deployTask, deployServiceEntity);
        } else {
            throw new InvalidServiceStateException(
                    String.format("Service %s is not in the state allowed for purging.",
                            deployServiceEntity.getId()));
        }
    }

    /**
     * Async method to purge deployed service.
     *
     * @param deployment          deployment
     * @param deployTask          deployTask
     * @param deployServiceEntity deployServiceEntity
     */
    @Async("taskExecutor")
    public void asyncPurgeService(Deployment deployment, DeployTask deployTask,
                                  DeployServiceEntity deployServiceEntity) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        if (!deployServiceEntity.getDeployResourceList().isEmpty()) {
            log.info("Purging created resources for service with ID: {}",
                    deployServiceEntity.getId());
            try {
                asyncDestroyService(deployment, deployTask);
            } catch (RuntimeException e) {
                log.error("Error purging created resources for service with ID: {}. Ignoring.",
                        deployServiceEntity.getId(), e);
            }
        } else {
            log.info("No resources to purge for service with ID: {}",
                    deployServiceEntity.getId());
        }
        deployServiceStorage.deleteDeployService(deployServiceEntity);
        log.info("Database entry with ID {} purged.", deployServiceEntity.getId());


    }

    /**
     * List deploy services with query model.
     *
     * @param query service query model.
     * @return serviceVos
     */
    public List<ServiceVo> listDeployedServices(ServiceQueryModel query) {
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        query.setUserId(userIdOptional.orElse(null));
        List<DeployServiceEntity> deployServices =
                deployServiceStorage.listServices(query);
        return deployServices.stream()
                .map(this::convertToServiceVo).toList();

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
            String errorMsg = String.format("Service with id %s not found.", id);
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }

        ServiceDetailVo serviceDetailVo = new ServiceDetailVo();
        serviceDetailVo.setServiceHostingType(
                deployServiceEntity.getDeployRequest().getServiceHostingType());
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

    /**
     * Callback method after deployment is complete.
     */
    public void deployCallback(String taskId, TerraformResult result) {
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(UUID.fromString(taskId));
        if (Objects.isNull(deployServiceEntity)) {
            String errorMsg = String.format("Service with id %s not found.", taskId);
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        DeployResult deployResult = handlerDeployResource(result);
        if (StringUtils.isNotBlank(result.getTerraformState())) {
            getResourceHandler(deployServiceEntity.getCsp()).handler(deployResult);
        }
        deployServiceEntity.setProperties(deployResult.getProperties());
        deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
        deployServiceEntity.getDeployResourceList().clear();
        deployServiceEntity.getDeployResourceList()
                .addAll(getDeployResourceEntityList(deployResult.getResources(),
                        deployServiceEntity));
        maskSensitiveFields(deployServiceEntity);
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        } else {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_FAILED);
            deployServiceEntity.setResultMessage(result.getCommandStdError());
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            rollbackOnDeploymentFailure(deployServiceEntity, deployResult,
                    deploymentMap.get(DeployerKind.TERRAFORM),
                    getDeployTask(taskId, deployServiceEntity));
        }
    }

    private DeployTask getDeployTask(String taskId, DeployServiceEntity deployServiceEntity) {
        DeployTask task = new DeployTask();
        task.setId(UUID.fromString(taskId));
        task.setOcl(deployServiceEntity.getDeployRequest().getOcl());
        task.setDeployRequest(deployServiceEntity.getDeployRequest());
        fillHandler(task);
        return task;
    }

    /**
     * Callback method after the service is destroyed.
     */
    public void destroyCallback(String taskId, TerraformResult result) {
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(UUID.fromString(taskId));
        if (Objects.isNull(deployServiceEntity)) {
            String errorMsg = String.format("Service with id %s not found.", taskId);
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROY_SUCCESS);
            deployServiceEntity.setProperties(new HashMap<>());
            deployServiceEntity.setPrivateProperties(new HashMap<>());
            deployResourceStorage.deleteByDeployServiceId(deployServiceEntity.getId());
        } else {
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROY_FAILED);
        }
    }

    private DeployResult handlerDeployResource(TerraformResult result) {
        DeployResult deployResult = new DeployResult();
        if (StringUtils.isBlank(result.getTerraformState())) {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_SUCCESS);
            deployResult.getPrivateProperties().put(STATE_FILE_NAME, result.getTerraformState());
            if (Objects.nonNull(result.getImportantFileContentMap())) {
                deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
            }
        }
        return deployResult;
    }

    private DeployResourceHandler getResourceHandler(Csp csp) {
        OrchestratorPlugin plugin =
                pluginManager.getPluginsMap().get(csp);
        if (Objects.isNull(plugin) || Objects.isNull(plugin.getResourceHandler())) {
            throw new PluginNotFoundException(
                    "Can't find suitable plugin and resource handler for the "
                            + "Task.");
        }
        return plugin.getResourceHandler();
    }

    private void fillHandler(DeployTask deployTask) {
        DeployResourceHandler resourceHandler =
                getResourceHandler(deployTask.getDeployRequest().getCsp());
        deployTask.setDeployResourceHandler(resourceHandler);
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

    private ServiceVo convertToServiceVo(DeployServiceEntity serviceEntity) {
        if (Objects.nonNull(serviceEntity)) {
            ServiceVo serviceVo = new ServiceVo();
            BeanUtils.copyProperties(serviceEntity, serviceVo);
            serviceVo.setServiceHostingType(
                    serviceEntity.getDeployRequest().getServiceHostingType());
            return serviceVo;
        }
        return null;
    }

    private void maskSensitiveFields(DeployServiceEntity deployServiceEntity) {
        log.debug("masking sensitive input data after deployment");
        if (Objects.nonNull(deployServiceEntity.getDeployRequest().getServiceRequestProperties())) {
            for (DeployVariable deployVariable
                    : deployServiceEntity.getDeployRequest().getOcl().getDeployment()
                    .getVariables()) {
                if (deployVariable.getSensitiveScope() != SensitiveScope.NONE
                        && (deployServiceEntity.getDeployRequest().getServiceRequestProperties()
                        .containsKey(deployVariable.getName()))) {
                    deployServiceEntity.getDeployRequest().getServiceRequestProperties()
                            .put(deployVariable.getName(), "********");

                }
            }
        }
    }

    /**
     * Deployment service.
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> deployService(UUID newId,
            String userId, DeployRequest deployRequest) {
        MDC.put(TASK_ID, newId.toString());
        log.info("start deploy service, service id : {}", newId);
        DeployTask deployTask = new DeployTask();
        deployRequest.setId(newId);
        deployTask.setId(newId);
        deployTask.setDeployRequest(deployRequest);
        Deployment deployment = getDeployHandler(deployTask);
        deployTask.getDeployRequest().setUserId(userId);
        deploy(deployment, deployTask);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Destroy service by deployed service id.
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> destroyService(String id) {
        MDC.put(TASK_ID, id);
        log.info("start destroy service, service id : {}", id);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString(id));
        Deployment deployment = getDestroyHandler(deployTask);
        destroy(deployment, deployTask);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Method to determine whether deploy is successful.
     */
    public boolean isDeploySuccess(UUID id) {
        MDC.put(TASK_ID, id.toString());
        log.info(" starting to poll for status update.. , service id : {}", id);
        ServiceDeploymentState deployState = null;
        while (deployState == ServiceDeploymentState.DEPLOYING || deployState == null) {
            deployState = deployServiceStorage.queryRefreshDeployServiceById(id)
                    .getServiceDeploymentState();
        }
        log.info("deployment status updated,state:{}", deployState);
        return deployState == ServiceDeploymentState.DEPLOY_SUCCESS;
    }

    /**
     * Method to determine whether destroy is successful.
     */
    @Async("taskExecutor")
    public CompletableFuture<Boolean> isDestroySuccess(UUID id) {
        ServiceDeploymentState destroyState = null;
        MDC.put(TASK_ID, id.toString());
        log.info(" starting to poll for status update.. , service id : {}", id);
        while (destroyState == ServiceDeploymentState.DESTROYING || destroyState == null) {
            destroyState = deployServiceStorage.findDeployServiceById(id)
                    .getServiceDeploymentState();
        }
        log.info("destroy status updated,state:{}", destroyState);
        return CompletableFuture.completedFuture(
                destroyState == ServiceDeploymentState.DESTROY_SUCCESS);
    }

    /**
     * Helper method to update service status in the database.
     *
     * @param id ID of the service
     * @param serviceDeploymentState new status to be set for the service.
     */
    public void updateServiceStatus(UUID id, ServiceDeploymentState serviceDeploymentState) {
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(id);
        deployServiceEntity.setServiceDeploymentState(serviceDeploymentState);
        deployServiceStorage.storeAndFlush(deployServiceEntity);
    }
}
