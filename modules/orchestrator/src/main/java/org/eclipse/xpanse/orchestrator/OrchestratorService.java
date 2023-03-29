/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import jakarta.annotation.Resource;
import java.io.File;
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
import org.eclipse.xpanse.modules.billing.BillingService;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.DeployTask;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.service.BillingDataResponse;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.eclipse.xpanse.modules.models.service.MonitorDataResponse;
import org.eclipse.xpanse.modules.models.service.MonitorResource;
import org.eclipse.xpanse.modules.models.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.view.ServiceVo;
import org.eclipse.xpanse.modules.monitor.Monitor;
import org.eclipse.xpanse.orchestrator.register.RegisterServiceStorage;
import org.eclipse.xpanse.orchestrator.service.DeployResourceStorage;
import org.eclipse.xpanse.orchestrator.service.DeployServiceStorage;
import org.eclipse.xpanse.orchestrator.utils.OpenApiUtil;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
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

    private final Map<Csp, Monitor> monitorServiceMap = new ConcurrentHashMap<>();

    private final Map<Csp, BillingService> billingServiceMap = new ConcurrentHashMap<>();

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
    private OpenApiUtil openApiUtil;

    @Value("${monitor.data.agent.enable}")
    private Boolean monitorAgentEnabled;

    /**
     * Get all OrchestratorPlugin implements group by Csp.
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
     * Get all Deployment implements group by DeployerKind.
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
     * Get all Monitor implements group by Csp.
     *
     * @return monitorMap
     */
    @Bean
    public Map<Csp, Monitor> monitorMap() {
        applicationContext.getBeansOfType(Monitor.class)
                .forEach((key, value) -> monitorServiceMap.put(value.getCsp(), value));
        return monitorServiceMap;
    }

    /**
     * Get all BillingService implements group by Csp.
     *
     * @return monitorMap
     */
    @Bean
    public Map<Csp, BillingService> billingServiceMap() {
        applicationContext.getBeansOfType(BillingService.class)
                .forEach((key, value) -> billingServiceMap.put(value.getCsp(), value));
        return billingServiceMap;
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
     * Method to monitor service.
     *
     * @param id Deploy service UUID.
     */
    public MonitorResource monitor(UUID id, String fromTime, String toTime) {
        // Find the deployed service.
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(deployServiceEntity) || Objects.isNull(
                deployServiceEntity.getCreateRequest()) || Objects.isNull(
                deployServiceEntity.getDeployResourceList())) {
            throw new RuntimeException(String.format("Deployed service with id %s not found",
                    id));
        }
        Csp csp = deployServiceEntity.getCreateRequest().getCsp();
        Monitor monitor = monitorServiceMap.get(csp);
        if (Objects.isNull(monitor)) {
            throw new RuntimeException("Can't find suitable monitor for the Task.");
        }
        MonitorResource monitorResource = new MonitorResource();
        List<MonitorDataResponse> cpu = monitor.cpuUsage(deployServiceEntity, monitorAgentEnabled,
                fromTime, toTime);
        List<MonitorDataResponse> mem = monitor.memUsage(deployServiceEntity, monitorAgentEnabled,
                fromTime, toTime);
        monitorResource.setCpu(cpu);
        monitorResource.setMem(mem);
        return monitorResource;

    }

    /**
     * Method to get service billing.
     *
     * @param id Deploy service UUID.
     */
    public List<BillingDataResponse> billing(UUID id, Boolean unit) {
        // Find the deployed service.
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(deployServiceEntity) || Objects.isNull(
                deployServiceEntity.getCreateRequest()) || Objects.isNull(
                deployServiceEntity.getDeployResourceList())) {
            throw new RuntimeException(String.format("Deployed service with id %s not found",
                    id));
        }
        Csp csp = deployServiceEntity.getCreateRequest().getCsp();

        BillingService billing = billingServiceMap.get(csp);
        if (Objects.isNull(billing)) {
            throw new RuntimeException("Can't find suitable billing for the Task.");
        }
        return billing.onDemandBilling(deployServiceEntity, unit);

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
            deployServiceEntity.setProperty(deployResult.getProperty());
            deployServiceEntity.setDeployResourceList(
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
            deployServiceEntity.setProperty(deployResult.getProperty());
            if (CollectionUtils.isEmpty(resources)) {
                deployResourceStorage.deleteByDeployServiceId(deployServiceEntity.getId());
            } else {
                deployServiceEntity.setDeployResourceList(
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
     * @return serviceDetailVo
     */
    public ServiceDetailVo getDeployServiceDetail(UUID id) {
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(deployServiceEntity)) {
            return null;
        }
        ServiceDetailVo serviceDetailVo = new ServiceDetailVo();
        BeanUtils.copyProperties(deployServiceEntity, serviceDetailVo);
        if (!CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
            List<DeployResource> deployResources =
                    EntityTransUtils.transResourceEntity(
                            deployServiceEntity.getDeployResourceList());
            serviceDetailVo.setDeployResources(deployResources);
        }
        return serviceDetailVo;
    }


    private void fillHandler(DeployTask deployTask) {
        // Find the deployment plugin and resource handler
        Csp csp = deployTask.getCreateRequest().getCsp();
        OrchestratorPlugin plugin = pluginMap.get(csp);

        if (Objects.isNull(plugin) || Objects.isNull(plugin.getResourceHandler())) {
            throw new RuntimeException(
                    "Can't find suitable plugin and resource handler for the "
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
        String rootPath = System.getProperty("user.dir");
        File folder = new File(rootPath + "/openapi");
        File file = new File(folder, uuid + ".html");
        if (file.exists()) {
            return "http://localhost:8080/openapi/" + uuid + ".html";
        } else {
            return openApiUtil.creatServiceApi(registerService);
        }
    }

    /**
     * delete OpenApi for registered service using the ID.
     *
     * @param id ID of registered service.
     */
    @Async("taskExecutor")
    public void deleteOpenApi(String id) {
        openApiUtil.deleteServiceApi(id);
    }

    /**
     * update OpenApi for registered service using the ID.
     *
     * @param id ID of registered service.
     */
    @Async("taskExecutor")
    public void updateOpenApi(String id) {
        UUID uuid = UUID.fromString(id);
        RegisterServiceEntity registerService = registerServiceStorage.getRegisterServiceById(uuid);
        if (Objects.isNull(registerService) || Objects.isNull(registerService.getOcl())) {
            throw new IllegalArgumentException(String.format("Registered service with id %s not "
                    + "existed.", id));
        }
        openApiUtil.updateServiceApi(registerService);
    }


}
