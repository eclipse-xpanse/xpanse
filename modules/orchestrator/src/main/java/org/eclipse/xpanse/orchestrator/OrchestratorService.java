/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import java.io.File;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    private final DeployResourceStorage deployResourceStorage;

    private final DeployVariableValidator deployVariableValidator;

    private final OpenApiUtil openApiUtil;

    @Getter
    private final List<Monitor> monitors = new ArrayList<>();

    @Getter
    private final List<Deployment> deployers = new ArrayList<>();

    @Getter
    private final List<OrchestratorPlugin> plugins = new ArrayList<>();
    @Getter
    private final List<BillingService> billings = new ArrayList<>();
    @Value("${monitor.data.agent.enable}")
    private Boolean monitorAgentEnabled;

    @Autowired
    OrchestratorService(RegisterServiceStorage registerServiceStorage,
            DeployServiceStorage deployServiceStorage,
            DeployResourceStorage deployResourceStorage,
            DeployVariableValidator deployVariableValidator,
            OpenApiUtil openApiUtil) {
        this.registerServiceStorage = registerServiceStorage;
        this.deployServiceStorage = deployServiceStorage;
        this.deployResourceStorage = deployResourceStorage;
        this.deployVariableValidator = deployVariableValidator;
        this.openApiUtil = openApiUtil;
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

            monitors.addAll(applicationContext.getBeansOfType(Monitor.class).values());
            if (monitors.isEmpty()) {
                log.warn("No monitor loaded by the runtime.");
            }

            billings.addAll(applicationContext.getBeansOfType(BillingService.class).values());
            if (billings.isEmpty()) {
                log.warn("No billing loaded by the runtime.");
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

        Optional<Monitor> monitorOptional =
                this.monitors.stream()
                        .filter(monitor -> monitor.getCsp() == csp)
                        .findFirst();
        if (monitorOptional.isEmpty()) {
            throw new RuntimeException("Can't find suitable monitor for the Task.");
        }
        Monitor monitor = monitorOptional.get();
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

        Optional<BillingService> billingOptional =
                this.billings.stream()
                        .filter(billing -> billing.getCsp() == csp)
                        .findFirst();
        if (billingOptional.isEmpty()) {
            throw new RuntimeException("Can't find suitable billing for the Task.");
        }
        BillingService billing = billingOptional.get();
        List<BillingDataResponse> billingDataResponseList = billing.onDemandBilling(
                deployServiceEntity, unit);
        return billingDataResponseList;

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
