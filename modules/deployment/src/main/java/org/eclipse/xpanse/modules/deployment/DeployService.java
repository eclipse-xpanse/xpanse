/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformResult;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceDetailsNotAccessible;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.manager.ServiceState;
import org.eclipse.xpanse.modules.models.service.query.ServiceQueryModel;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.eclipse.xpanse.modules.orchestrator.manage.ServiceManagerRequest;
import org.eclipse.xpanse.modules.policy.policyman.PolicyManager;
import org.eclipse.xpanse.modules.policy.policyman.UserPolicyManager;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
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

    public static final String STATE_FILE_NAME = "terraform.tfstate";
    private static final String TASK_ID = "TASK_ID";
    private final Map<DeployerKind, Deployment> deploymentMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private DeployServiceStorage deployServiceStorage;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private IdentityProviderManager identityProviderManager;
    @Resource
    private AesUtil aesUtil;
    @Resource
    private ServiceVariablesJsonSchemaValidator serviceVariablesJsonSchemaValidator;
    @Resource
    private PolicyManager policyManager;
    @Resource
    private UserPolicyManager userPolicyManager;

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
     * Create new deploy task by deploy request.
     *
     * @param deployRequest deploy request.
     * @return new deploy task.
     */
    public DeployTask createNewDeployTask(DeployRequest deployRequest) {
        // Find the registered service template and fill Ocl.
        ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setName(StringUtils.lowerCase(deployRequest.getServiceName()));
        serviceTemplate.setVersion(StringUtils.lowerCase(deployRequest.getVersion()));
        serviceTemplate.setCsp(deployRequest.getCsp());
        serviceTemplate.setCategory(deployRequest.getCategory());
        serviceTemplate.setServiceHostingType(deployRequest.getServiceHostingType());
        serviceTemplate = serviceTemplateStorage.findServiceTemplate(serviceTemplate);
        if (Objects.isNull(serviceTemplate) || Objects.isNull(serviceTemplate.getOcl())) {
            throw new ServiceTemplateNotRegistered("Service template not found.");
        }

        // Check context validation
        if (Objects.nonNull(serviceTemplate.getOcl().getDeployment()) && Objects.nonNull(
                deployRequest.getServiceRequestProperties())) {
            List<DeployVariable> deployVariables = serviceTemplate.getOcl().getDeployment()
                    .getVariables();

            serviceVariablesJsonSchemaValidator.validateDeployVariables(deployVariables,
                    deployRequest.getServiceRequestProperties(),
                    serviceTemplate.getJsonObjectSchema());
        }
        encodeDeployVariable(serviceTemplate,
                deployRequest.getServiceRequestProperties());

        if (StringUtils.isEmpty(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        // Create new deploy task by deploy request.
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployRequest.setOcl(serviceTemplate.getOcl());
        deployTask.setDeployRequest(deployRequest);
        deployTask.setNamespace(serviceTemplate.getNamespace());
        deployTask.setOcl(serviceTemplate.getOcl());
        // Fill the handler
        fillHandler(deployTask);
        // Fill the service policies
        fillServicePolicies(deployTask, serviceTemplate);
        return deployTask;
    }

    private void fillServicePolicies(DeployTask deployTask, ServiceTemplateEntity serviceTemplate) {
        if (Objects.nonNull(serviceTemplate)
                && !CollectionUtils.isEmpty(serviceTemplate.getServicePolicyList())) {
            List<ServicePolicy> servicePolicies = serviceTemplate.getServicePolicyList().stream()
                    .filter(servicePolicyEntity -> servicePolicyEntity.getEnabled()
                            && StringUtils.isNotBlank(servicePolicyEntity.getPolicy()))
                    .map(servicePolicyEntity -> {
                        ServicePolicy servicePolicy = new ServicePolicy();
                        BeanUtils.copyProperties(servicePolicyEntity, servicePolicy);
                        servicePolicy.setServiceTemplateId(
                                servicePolicyEntity.getServiceTemplate().getId());
                        return servicePolicy;
                    }).toList();
            deployTask.setServicePolicies(servicePolicies);
        }
    }

    private List<UserPolicy> getUserPolicies(DeployTask deployTask) {
        if (Objects.nonNull(deployTask.getDeployRequest())) {
            String userId = deployTask.getDeployRequest().getUserId();
            Csp csp = deployTask.getDeployRequest().getCsp();
            UserPolicyQueryRequest queryRequest = new UserPolicyQueryRequest();
            queryRequest.setUserId(userId);
            queryRequest.setCsp(csp);
            queryRequest.setEnabled(true);
            return userPolicyManager.listUserPolicies(queryRequest);
        }
        return null;
    }


    /**
     * Validate deployment with policies.
     *
     * @param deployment deployment.
     * @param deployTask deploy task.
     */
    public void validateDeploymentWithPolicies(Deployment deployment, DeployTask deployTask) {

        List<ServicePolicy> servicePolicies = deployTask.getServicePolicies();
        List<UserPolicy> userPolicies = getUserPolicies(deployTask);
        if (CollectionUtils.isEmpty(userPolicies) && CollectionUtils.isEmpty(servicePolicies)) {
            return;
        }

        String planJson = deployment.getDeployPlanAsJson(deployTask);
        if (StringUtils.isEmpty(planJson)) {
            return;
        }

        evaluateDeploymentPlanWithServicePolicies(servicePolicies, planJson);

        evaluateDeploymentPlanWithUserPolicies(userPolicies, planJson);
    }

    private void evaluateDeploymentPlanWithServicePolicies(List<ServicePolicy> servicePolicies,
                                                           String planJson) {
        if (!CollectionUtils.isEmpty(servicePolicies)) {
            List<String> servicePolicyList = servicePolicies.stream()
                    .map(ServicePolicy::getPolicy).toList();
            String errMsg = "Evaluate deployment plan with service policies failed.";
            EvalResult evalResult = policyManager.evaluatePolicies(servicePolicyList, planJson);
            if (!evalResult.getIsSuccessful()) {
                ServicePolicy failedServicePolicy = servicePolicies.stream()
                        .filter(servicePolicy -> servicePolicy.getPolicy()
                                .equals(evalResult.getPolicy()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(failedServicePolicy)) {
                    errMsg = String.format(errMsg + "\n Failed by the policy with id: %s."
                                    + "\n Deployment plan: %s",
                            failedServicePolicy.getId(), planJson);
                } else {
                    errMsg = String.format(errMsg + "\n Failed by the policy with context: %s."
                                    + "\nDeployment plan: %s",
                            evalResult.getPolicy(), planJson);
                }
                log.error(errMsg);
                throw new PoliciesEvaluationFailedException(errMsg);
            } else {
                log.info("Evaluate deployment plan with service policies successful.");
            }
        }
    }


    private void evaluateDeploymentPlanWithUserPolicies(List<UserPolicy> userPolicies,
                                                        String planJson) {
        if (!CollectionUtils.isEmpty(userPolicies)) {
            String errMsg = "Evaluate deployment plan with user policies failed.";
            List<String> userPolicyList = userPolicies.stream()
                    .map(UserPolicy::getPolicy).toList();
            EvalResult evalResult = policyManager.evaluatePolicies(userPolicyList, planJson);
            if (!evalResult.getIsSuccessful()) {
                UserPolicy failedUserPolicy = userPolicies.stream()
                        .filter(userPolicy -> userPolicy.getPolicy()
                                .equals(evalResult.getPolicy()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(failedUserPolicy)) {
                    errMsg = String.format(errMsg + "\n Failed by the policy with id: %s."
                                    + "\n Deployment plan: %s",
                            failedUserPolicy.getId(), planJson);
                } else {
                    errMsg = String.format(errMsg + "\n Failed by the policy with context: %s."
                                    + "\nDeployment plan: %s",
                            evalResult.getPolicy(), planJson);
                }
                log.error(errMsg);
                throw new PoliciesEvaluationFailedException(errMsg);
            } else {
                log.info("Evaluate deployment plan with user policies successful.");
            }
        }
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

    private String generateCustomerServiceName(DeployRequest deployRequest) {
        if (deployRequest.getServiceName().length() > 5) {
            return deployRequest.getServiceName().substring(0, 4) + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        } else {
            return deployRequest.getServiceName() + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        }
    }

    private void fillHandler(DeployTask deployTask) {
        DeployResourceHandler resourceHandler =
                getResourceHandler(deployTask.getDeployRequest().getCsp());
        deployTask.setDeployResourceHandler(resourceHandler);
    }

    private DeployServiceEntity storeNewDeployServiceEntity(DeployTask deployTask) {
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
        entity.setNamespace(deployTask.getNamespace());
        entity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        DeployServiceEntity storedEntity = deployServiceStorage.storeAndFlush(entity);
        if (Objects.isNull(storedEntity)) {
            log.error("Store new deploy service entity with id:{} failed.", deployTask.getId());
            throw new RuntimeException("Store new deploy service entity failed.");
        }
        return storedEntity;
    }

    /**
     * Async method to deploy service.
     *
     * @param deployment deployment
     * @param deployTask deployTask
     */
    public void deployService(Deployment deployment, DeployTask deployTask) {
        deploy(deployment, deployTask);
    }

    private void deploy(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployResult deployResult;
        DeployServiceEntity storedEntity = null;
        try {
            storedEntity = storeNewDeployServiceEntity(deployTask);
            validateDeploymentWithPolicies(deployment, deployTask);
            deployResult = deployment.deploy(deployTask);
        } catch (RuntimeException e) {
            log.info("Deploy service with id:{} failed.", deployTask.getId(), e);
            deployResult = new DeployResult();
            deployResult.setId(deployTask.getId());
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
            deployResult.setMessage(e.getMessage());
        }
        try {
            DeployServiceEntity updatedDeployServiceEntity =
                    flushDeployServiceEntity(deployResult, storedEntity);
            if (ServiceDeploymentState.DEPLOY_FAILED
                    == updatedDeployServiceEntity.getServiceDeploymentState()) {
                rollbackOnDeploymentFailure(deployment, deployTask, updatedDeployServiceEntity);
            }
        } catch (RuntimeException e) {
            log.info("Deploy service with id:{} update database entity failed.",
                    deployTask.getId(), e);
        }
    }


    private DeployServiceEntity flushDeployServiceEntity(DeployResult deployResult,
                                                         DeployServiceEntity storedEntity)
            throws RuntimeException {
        if (Objects.nonNull(deployResult.getState()) && Objects.nonNull(storedEntity)) {
            log.info("Deploy task update deploy service entity with id:{}", deployResult.getId());
            DeployServiceEntity deployServiceEntityToFlush = new DeployServiceEntity();
            BeanUtils.copyProperties(storedEntity, deployServiceEntityToFlush);
            if (TerraformExecState.DEPLOY_SUCCESS == deployResult.getState()) {
                deployServiceEntityToFlush.setServiceDeploymentState(
                        ServiceDeploymentState.DEPLOY_SUCCESS);
                deployServiceEntityToFlush.setServiceState(ServiceState.RUNNING);
            } else {
                deployServiceEntityToFlush.setServiceDeploymentState(
                        ServiceDeploymentState.DEPLOY_FAILED);
                deployServiceEntityToFlush.setServiceState(ServiceState.NOT_RUNNING);
                deployServiceEntityToFlush.setResultMessage(deployResult.getMessage());
            }
            updateDeployResourceEntity(deployResult, deployServiceEntityToFlush);
            return deployServiceStorage.storeAndFlush(deployServiceEntityToFlush);
        } else {
            return storedEntity;
        }
    }

    private void updateDeployResourceEntity(DeployResult deployResult,
                                            DeployServiceEntity deployServiceEntity) {

        if (!CollectionUtils.isEmpty(deployServiceEntity.getProperties())) {
            deployServiceEntity.getProperties().clear();
        }
        if (!CollectionUtils.isEmpty(deployServiceEntity.getPrivateProperties())) {
            deployServiceEntity.getPrivateProperties().clear();
        }
        if (!CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
            deployServiceEntity.getDeployResourceList().clear();
        }

        if (!CollectionUtils.isEmpty(deployResult.getPrivateProperties())) {
            deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
        }
        if (!CollectionUtils.isEmpty(deployResult.getProperties())) {
            deployServiceEntity.setProperties(deployResult.getProperties());
        }
        if (!CollectionUtils.isEmpty(deployResult.getResources())) {
            deployServiceEntity.setDeployResourceList(
                    getDeployResourceEntityList(deployResult.getResources(),
                            deployServiceEntity));
        }
        maskSensitiveFields(deployServiceEntity);
    }

    /**
     * Convert deploy resources to deploy resource entities.
     */
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
     * Perform rollback when deployment fails and destroy the created resources.
     */
    private void rollbackOnDeploymentFailure(Deployment deployment,
                                             DeployTask deployTask,
                                             DeployServiceEntity deployServiceEntity) {
        log.info("Performing rollback of already provisioned resources.");
        if (Objects.nonNull(deployServiceEntity.getDeployResourceList())
                && !deployServiceEntity.getDeployResourceList().isEmpty()) {
            log.info("destroying created resources for service with ID: {}", deployTask.getId());
            destroy(deployment, deployTask, deployServiceEntity, true);
        }
    }

    /**
     * Async method to destroy service.
     *
     * @param deployment          deployment
     * @param deployTask          deployTask
     * @param deployServiceEntity deployServiceEntity
     */
    public void destroyService(Deployment deployment, DeployTask deployTask,
                                    DeployServiceEntity deployServiceEntity) {
        destroy(deployment, deployTask, deployServiceEntity, false);
    }

    private void destroy(Deployment deployment, DeployTask destroyTask,
                         DeployServiceEntity deployServiceEntity, boolean isCalledWhenRollback) {
        MDC.put(TASK_ID, destroyTask.getId().toString());
        DeployResult destroyResult;
        try {
            String stateFile = getStoredStateContent(deployServiceEntity);
            destroyResult = deployment.destroy(destroyTask, stateFile);
        } catch (RuntimeException e) {
            log.info("Destroy service with id:{} failed.", destroyTask.getId(), e);
            destroyResult = new DeployResult();
            destroyResult.setId(destroyTask.getId());
            destroyResult.setState(TerraformExecState.DESTROY_FAILED);
            destroyResult.setState(TerraformExecState.DESTROY_FAILED);
            destroyResult.setMessage(e.getMessage());
        }

        try {
            DeployServiceEntity updatedDeployServiceEntity =
                    flushDestroyServiceEntity(destroyResult, deployServiceEntity,
                            isCalledWhenRollback);
            if (ServiceDeploymentState.DESTROY_SUCCESS
                    == updatedDeployServiceEntity.getServiceDeploymentState()
                    || ServiceDeploymentState.DEPLOY_FAILED
                    == updatedDeployServiceEntity.getServiceDeploymentState()) {
                deployment.deleteTaskWorkspace(destroyTask.getId().toString());
            }
        } catch (RuntimeException e) {
            log.info("Destroy service with id:{} update database entity failed.",
                    destroyTask.getId(), e);
        }
    }

    private String getStoredStateContent(DeployServiceEntity deployServiceEntity) {
        if (Objects.isNull(deployServiceEntity)
                || CollectionUtils.isEmpty(deployServiceEntity.getPrivateProperties())
                || StringUtils.isEmpty(
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME))) {
            throw new ServiceNotDeployedException(
                    "Can't find valid state context in stored deployed service.");
        }
        return deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
    }

    private DeployServiceEntity flushDestroyServiceEntity(DeployResult destroyResult,
                                                          DeployServiceEntity deployServiceEntity,
                                                          boolean isCalledWhenRollback)
            throws RuntimeException {
        if (Objects.nonNull(destroyResult) && Objects.nonNull(destroyResult.getState())) {
            log.info("Update stored deploy service entity by result of destroy task with id:{}",
                    destroyResult.getId());
            DeployServiceEntity deployServiceEntityToFlush = new DeployServiceEntity();
            BeanUtils.copyProperties(deployServiceEntity, deployServiceEntityToFlush);
            if (isCalledWhenRollback) {
                if (destroyResult.getState() == TerraformExecState.DESTROY_SUCCESS) {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.DEPLOY_FAILED);
                    deployServiceEntityToFlush.setServiceState(ServiceState.NOT_RUNNING);
                } else {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.ROLLBACK_FAILED);
                    deployServiceEntityToFlush.setServiceState(ServiceState.RUNNING);
                }
            } else {
                if (destroyResult.getState() == TerraformExecState.DESTROY_SUCCESS) {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.DESTROY_SUCCESS);
                    deployServiceEntityToFlush.setServiceState(ServiceState.NOT_RUNNING);
                } else {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.DESTROY_FAILED);
                    deployServiceEntityToFlush.setServiceState(ServiceState.RUNNING);
                    deployServiceEntityToFlush.setResultMessage(destroyResult.getMessage());
                }
            }
            updateDeployResourceEntity(destroyResult, deployServiceEntityToFlush);
            return deployServiceStorage.storeAndFlush(deployServiceEntityToFlush);
        } else {
            return deployServiceEntity;
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
        purgeService(deployment, deployTask, deployServiceEntity);
    }

    /**
     * purge the service based on the serviceDeploymentState.
     *
     * @param deployment          deployment
     * @param deployTask          deployTask
     * @param deployServiceEntity deployServiceEntity
     */
    private void purgeService(Deployment deployment, DeployTask deployTask,
                              DeployServiceEntity deployServiceEntity) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        try {
            if (Objects.nonNull(deployServiceEntity.getDeployResourceList())
                    && !deployServiceEntity.getDeployResourceList().isEmpty()) {
                log.info("destroying created resources for service with ID: {}",
                        deployTask.getId());
                destroy(deployment, deployTask, deployServiceEntity, false);
            }
            deployServiceStorage.deleteDeployService(deployServiceEntity);
            log.info("Database entry with ID {} purged.", deployServiceEntity.getId());
        } catch (RuntimeException e) {
            log.error("Error purging created resources for service with ID: {}. Ignoring.",
                    deployTask.getId(), e);
        }
    }

    /**
     * List deploy services with query model.
     *
     * @param query service query model.
     * @return serviceVos
     */
    public List<DeployedService> listDeployedServices(ServiceQueryModel query) {
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        query.setUserId(userIdOptional.orElse(null));
        List<DeployServiceEntity> deployServices =
                deployServiceStorage.listServices(query);
        return deployServices.stream()
                .map(this::convertToDeployedService).toList();

    }

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return serviceDetailVo
     */
    public DeployedServiceDetails getSelfHostedServiceDetailsByIdForEndUser(UUID id) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(id);
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        ServiceHostingType serviceHostingType =
                deployServiceEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SELF != serviceHostingType) {
            String errorMsg = String.format("details of non service-self hosted with id %s is not "
                    + "accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        return EntityTransUtils.transDeployServiceEntityToServiceDetailVo(deployServiceEntity);
    }

    /**
     * Get vendor hosted service detail by id.
     *
     * @param id ID of deploy service.
     * @return VendorHostedDeployedServiceDetails
     */
    public VendorHostedDeployedServiceDetails getVendorHostedServiceDetailsByIdForEndUser(UUID id) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(id);
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        ServiceHostingType serviceHostingType =
                deployServiceEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg = String.format(
                    "details of non service-vendor hosted with id %s is not accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        return EntityTransUtils.transServiceEntityToVendorHostedServiceDetailsVo(
                deployServiceEntity);
    }

    /**
     * Callback method after deployment is complete.
     */
    public void deployCallback(String taskId, TerraformResult result) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(UUID.fromString(taskId));
        DeployResult deployResult = handlerCallbackDeployResult(result);
        deployResult.setId(UUID.fromString(taskId));
        if (StringUtils.isNotBlank(result.getTerraformState())) {
            getResourceHandler(deployServiceEntity.getCsp()).handler(deployResult);
        }
        DeployServiceEntity updatedDeployServiceEntity =
                flushDeployServiceEntity(deployResult, deployServiceEntity);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedDeployServiceEntity.getServiceDeploymentState()) {
            DeployTask deployTask = getDeployTaskByStoredService(updatedDeployServiceEntity);
            Deployment deployment = getDeployment(updatedDeployServiceEntity.getDeployRequest()
                    .getOcl().getDeployment().getKind());
            rollbackOnDeploymentFailure(deployment, deployTask, updatedDeployServiceEntity);
        }

    }

    private DeployResult handlerCallbackDeployResult(TerraformResult result) {
        DeployResult deployResult = new DeployResult();
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployResult.setState(TerraformExecState.DEPLOY_SUCCESS);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
            deployResult.setMessage(result.getCommandStdError());
        }
        deployResult.getPrivateProperties().put(STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }

    /**
     * Callback method after the service is destroyed.
     */
    public void destroyCallback(String taskId, TerraformResult result) {
        log.info("Update database entity with id:{} with destroy  callback result.", taskId);
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(UUID.fromString(taskId));
        DeployResult destroyResult = handlerCallbackDestroyResult(result);
        destroyResult.setId(UUID.fromString(taskId));
        if (StringUtils.isNotBlank(result.getTerraformState())) {
            getResourceHandler(deployServiceEntity.getCsp()).handler(destroyResult);
        }
        try {
            flushDestroyServiceEntity(destroyResult, deployServiceEntity, false);
        } catch (RuntimeException e) {
            log.info("Update database entity with id:{} with destroy callback result failed.",
                    taskId, e);
        }
    }

    private DeployResult handlerCallbackDestroyResult(TerraformResult result) {
        DeployResult deployResult = new DeployResult();
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployResult.setState(TerraformExecState.DESTROY_SUCCESS);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
            deployResult.setMessage(result.getCommandStdError());
        }
        deployResult.getPrivateProperties().put(STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }

    private DeployResourceHandler getResourceHandler(Csp csp) {
        OrchestratorPlugin plugin =
                pluginManager.getPluginsMap().get(csp);
        if (Objects.isNull(plugin) || Objects.isNull(plugin.getResourceHandler())) {
            throw new PluginNotFoundException(
                    "Can't find suitable plugin and resource handler for the Task.");
        }
        return plugin.getResourceHandler();
    }

    /**
     * Get Deployment bean available for the requested DeployerKind.
     *
     * @param deployerKind kind of deployer.
     * @return Deployment bean for the provided deployerKind.
     */
    public Deployment getDeployment(DeployerKind deployerKind) {
        Deployment deployment = deploymentMap.get(deployerKind);
        if (Objects.isNull(deployment)) {
            throw new DeployerNotFoundException("Can't find suitable deployer for the Task.");
        }
        return deployment;
    }

    private DeployedService convertToDeployedService(DeployServiceEntity serviceEntity) {
        if (Objects.nonNull(serviceEntity)) {
            DeployedService deployedService = new DeployedService();
            BeanUtils.copyProperties(serviceEntity, deployedService);
            deployedService.setServiceHostingType(
                    serviceEntity.getDeployRequest().getServiceHostingType());
            return deployedService;
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
     *
     * @param newId         new service id.
     * @param userId        user id.
     * @param deployRequest deploy request.
     * @return new deployed service entity.
     */
    @Async("taskExecutor")
    public CompletableFuture<DeployServiceEntity> deployServiceById(UUID newId,
                                                                String userId,
                                                                DeployRequest deployRequest) {
        MDC.put(TASK_ID, newId.toString());
        log.info("Migrate workflow start deploy new service with id: {}", newId);
        DeployTask deployTask = createNewDeployTask(deployRequest);
        // override task id and user id.
        deployTask.setId(newId);
        deployTask.getDeployRequest().setUserId(userId);
        Deployment deployment = getDeployment(deployTask.getOcl().getDeployment().getKind());
        storeNewDeployServiceEntity(deployTask);
        deploy(deployment, deployTask);
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(newId);
        return CompletableFuture.completedFuture(deployServiceEntity);
    }

    /**
     * Destroy service by deployed service id.
     */
    @Async("taskExecutor")
    public CompletableFuture<DeployServiceEntity> destroyServiceById(String id) {
        MDC.put(TASK_ID, id);
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(UUID.fromString(id));
        DeployTask deployTask = getDeployTaskByStoredService(deployServiceEntity);
        Deployment deployment = getDeployment(deployTask.getOcl().getDeployment().getKind());
        destroy(deployment, deployTask, deployServiceEntity, false);
        return CompletableFuture.completedFuture(deployServiceEntity);
    }

    /**
     * Use query model to list SV deployment services.
     *
     * @param query service query model.
     * @return serviceVos
     */
    public List<DeployedService> listDeployedServicesOfIsv(ServiceQueryModel query) {
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        return namespace.map(s -> deployServiceStorage.listServices(query).stream()
                .filter(deployServiceEntity -> s
                        .equals(deployServiceEntity.getNamespace()))
                .map(this::convertToDeployedService).toList()).orElseGet(ArrayList::new);
    }

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return serviceDetailVo
     */
    public DeployedServiceDetails getServiceDetailsByIdForIsv(UUID id) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(id);
        ServiceHostingType serviceHostingType =
                deployServiceEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg = String.format("the details of Service with id %s no accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        if (namespace.isEmpty() || !namespace.get().equals(deployServiceEntity.getNamespace())) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        return EntityTransUtils.transDeployServiceEntityToServiceDetailVo(deployServiceEntity);
    }


    /**
     * Get destroy task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getDestroyTask(DeployServiceEntity deployServiceEntity) {

        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (state.equals(ServiceDeploymentState.DEPLOYING)
                || state.equals(ServiceDeploymentState.DESTROYING)) {
            throw new InvalidServiceStateException(String.format("Service with id %s is %s.",
                    deployServiceEntity.getId(), state));
        }
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
        DeployServiceEntity updatedDeployServiceEntity =
                deployServiceStorage.storeAndFlush(deployServiceEntity);
        return getDeployTaskByStoredService(updatedDeployServiceEntity);
    }

    private DeployTask getDeployTaskByStoredService(DeployServiceEntity deployServiceEntity) {
        // Set Ocl and CreateRequest
        DeployTask deployTask = new DeployTask();
        deployTask.setId(deployServiceEntity.getId());
        deployTask.setDeployRequest(deployServiceEntity.getDeployRequest());
        deployTask.setOcl(deployServiceEntity.getDeployRequest().getOcl());
        fillHandler(deployTask);
        return deployTask;

    }

    /**
     * Get purge task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getPurgeTask(DeployServiceEntity deployServiceEntity) {
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (!(state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_SUCCESS
                || state == ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s is not in the state allowed for purging.",
                            deployServiceEntity.getId()));
        }
        return getDeployTaskByStoredService(deployServiceEntity);
    }


    /**
     * Get deploy service entity by id.
     *
     * @param id task id.
     * @return deploy service entity.
     */
    public DeployServiceEntity getDeployServiceEntity(UUID id) {
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(deployServiceEntity)) {
            String errorMsg = String.format("Service with id %s not found.", id);
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        return deployServiceEntity;
    }

    /**
     * Start the service by the deployed service id.
     *
     * @param id service id.
     * @return deployedService.
     */
    public DeployedService startService(UUID id) {
        MDC.put(TASK_ID, id.toString());
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(id);
        try {
            validateStartDeployServiceEntity(deployServiceEntity);
            deployServiceEntity.setServiceState(ServiceState.STARTING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            if (start(deployServiceEntity)) {
                deployServiceEntity.setLastStartedAt(OffsetDateTime.now());
                deployServiceEntity.setServiceState(ServiceState.RUNNING);
            } else {
                deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            }
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        } catch (RuntimeException e) {
            log.info("start service by service id:{} failed.", id);
            deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        }
        DeployedService deployedService = new DeployedService();
        BeanUtils.copyProperties(deployServiceEntity, deployedService);
        deployedService.setServiceHostingType(deployServiceEntity.getDeployRequest()
                .getServiceHostingType());
        return deployedService;
    }

    /**
     * Stop the service by the deployed service id.
     *
     * @param id service id.
     * @return deployedService.
     */
    public DeployedService stopService(UUID id) {
        MDC.put(TASK_ID, id.toString());
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(id);
        try {
            validateStopDeployServiceEntity(deployServiceEntity);
            deployServiceEntity.setServiceState(ServiceState.STOPPING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            if (stop(deployServiceEntity)) {
                deployServiceEntity.setLastStoppedAt(OffsetDateTime.now());
                deployServiceEntity.setServiceState(ServiceState.STOPPED);
            } else {
                deployServiceEntity.setServiceState(ServiceState.STOPPING_FAILED);
            }
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        } catch (RuntimeException e) {
            log.info("stop service by service id:{} failed.", id);
            deployServiceEntity.setServiceState(ServiceState.STOPPING_FAILED);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        }
        DeployedService deployedService = new DeployedService();
        BeanUtils.copyProperties(deployServiceEntity, deployedService);
        deployedService.setServiceHostingType(deployServiceEntity.getDeployRequest()
                .getServiceHostingType());
        return deployedService;
    }

    /**
     * Restart the service by the deployed service id.
     *
     * @param id service id.
     * @return deployedService.
     */
    public DeployedService restartService(UUID id) {
        MDC.put(TASK_ID, id.toString());
        DeployServiceEntity deployServiceEntity = getDeployServiceEntity(id);
        try {
            validateDeployServiceEntity(deployServiceEntity);
            deployServiceEntity.setServiceState(ServiceState.STARTING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            if (restart(deployServiceEntity)) {
                deployServiceEntity.setServiceState(ServiceState.RUNNING);
            } else {
                deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            }
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        } catch (RuntimeException e) {
            log.info("stop service by service id:{} failed.", id);
            deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
        }
        DeployedService deployedService = new DeployedService();
        BeanUtils.copyProperties(deployServiceEntity, deployedService);
        deployedService.setServiceHostingType(deployServiceEntity.getDeployRequest()
                .getServiceHostingType());
        return deployedService;
    }

    private boolean start(DeployServiceEntity deployServiceEntity) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(deployServiceEntity.getCsp());
        List<DeployResourceEntity> deployResourceList =
                getVmDeployResourceEntities(deployServiceEntity);
        ServiceManagerRequest serviceManagerRequest =
                getServiceManagerRequest(deployResourceList,
                        deployServiceEntity.getDeployRequest().getServiceHostingType(),
                        deployServiceEntity.getUserId());
        return plugin.startService(serviceManagerRequest);
    }

    private boolean stop(DeployServiceEntity deployServiceEntity) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(deployServiceEntity.getCsp());
        List<DeployResourceEntity> deployResourceList =
                getVmDeployResourceEntities(deployServiceEntity);
        ServiceManagerRequest serviceManagerRequest =
                getServiceManagerRequest(deployResourceList,
                        deployServiceEntity.getDeployRequest().getServiceHostingType(),
                        deployServiceEntity.getUserId());
        return plugin.stopService(serviceManagerRequest);
    }

    private boolean restart(DeployServiceEntity deployServiceEntity) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(deployServiceEntity.getCsp());
        List<DeployResourceEntity> deployResourceList =
                getVmDeployResourceEntities(deployServiceEntity);
        ServiceManagerRequest serviceManagerRequest =
                getServiceManagerRequest(deployResourceList,
                        deployServiceEntity.getDeployRequest().getServiceHostingType(),
                        deployServiceEntity.getUserId());
        return plugin.restartService(serviceManagerRequest);
    }

    private void validateStartDeployServiceEntity(DeployServiceEntity deployServiceEntity) {
        validateDeployServiceEntity(deployServiceEntity);
        if (deployServiceEntity.getServiceState() == ServiceState.RUNNING
                || deployServiceEntity.getServiceState() == ServiceState.STOPPING_FAILED) {
            return;
        }
        if (!(deployServiceEntity.getServiceState() == ServiceState.STOPPED
                || deployServiceEntity.getServiceState() == ServiceState.STARTING_FAILED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s is not allowed to start. serviceState: %s",
                            deployServiceEntity.getId(), deployServiceEntity.getServiceState()));
        }
    }

    private void validateStopDeployServiceEntity(DeployServiceEntity deployServiceEntity) {
        validateDeployServiceEntity(deployServiceEntity);
        if (deployServiceEntity.getServiceState() == ServiceState.STOPPED
                || deployServiceEntity.getServiceState() == ServiceState.STARTING_FAILED) {
            return;
        }
        if (!(deployServiceEntity.getServiceState() == ServiceState.RUNNING
                || deployServiceEntity.getServiceState() == ServiceState.STOPPING_FAILED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s is not allowed to stop. serviceState: %s",
                            deployServiceEntity.getId(), deployServiceEntity.getServiceState()));
        }
    }

    private void validateDeployServiceEntity(DeployServiceEntity deployServiceEntity) {
        ServiceDeploymentState serviceDeploymentState =
                deployServiceEntity.getServiceDeploymentState();
        if (!(serviceDeploymentState == ServiceDeploymentState.DEPLOY_SUCCESS
                || serviceDeploymentState == ServiceDeploymentState.DESTROY_FAILED)) {
            throw new InvalidServiceStateException(String.format("Service with id %s is %s.",
                    deployServiceEntity.getId(), serviceDeploymentState));
        }
        if (deployServiceEntity.getDeployRequest().getServiceHostingType()
                == ServiceHostingType.SERVICE_VENDOR) {
            return;
        }
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to start service by service id.");
        }
    }

    private List<DeployResourceEntity> getVmDeployResourceEntities(
            DeployServiceEntity deployServiceEntity) {
        return deployServiceEntity.getDeployResourceList().stream()
                .filter(deployResourceEntity -> deployResourceEntity.getKind()
                        .equals(DeployResourceKind.VM)).collect(Collectors.toList());
    }


    private ServiceManagerRequest getServiceManagerRequest(
            List<DeployResourceEntity> deployResourceList, ServiceHostingType serviceHostingType,
            String userId) {
        ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setDeployResourceEntityList(deployResourceList);
        if (serviceHostingType == ServiceHostingType.SELF) {
            serviceManagerRequest.setUserId(userId);
        }
        serviceManagerRequest.setRegionName(
                deployResourceList.get(0).getProperties().get("region"));
        return serviceManagerRequest;
    }
}
