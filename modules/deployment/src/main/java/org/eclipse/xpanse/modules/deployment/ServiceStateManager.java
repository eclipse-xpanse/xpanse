/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Bean to manage service state.
 */
@Component
@Slf4j
public class ServiceStateManager {

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private IdentityProviderManager identityProviderManager;

    /**
     * Start the service by the deployed service id.
     *
     * @param id service id.
     * @return deployedService.
     */
    public DeployedService startService(UUID id) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        try {
            validateStartDeployServiceEntity(deployServiceEntity);
            deployServiceEntity.setServiceState(ServiceState.STARTING);
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
            if (start(deployServiceEntity)) {
                deployServiceEntity.setLastStartedAt(OffsetDateTime.now());
                deployServiceEntity.setServiceState(ServiceState.RUNNING);
            } else {
                deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            }
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
        } catch (RuntimeException e) {
            log.info("start service by service id:{} failed.", id);
            deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
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
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        try {
            validateStopDeployServiceEntity(deployServiceEntity);
            deployServiceEntity.setServiceState(ServiceState.STOPPING);
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
            if (stop(deployServiceEntity)) {
                deployServiceEntity.setLastStoppedAt(OffsetDateTime.now());
                deployServiceEntity.setServiceState(ServiceState.STOPPED);
            } else {
                deployServiceEntity.setServiceState(ServiceState.STOPPING_FAILED);
            }
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
        } catch (RuntimeException e) {
            log.info("stop service by service id:{} failed.", id);
            deployServiceEntity.setServiceState(ServiceState.STOPPING_FAILED);
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
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
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        try {
            validateDeployServiceEntity(deployServiceEntity);
            deployServiceEntity.setServiceState(ServiceState.STARTING);
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
            if (restart(deployServiceEntity)) {
                deployServiceEntity.setServiceState(ServiceState.RUNNING);
            } else {
                deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            }
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
        } catch (RuntimeException e) {
            log.info("stop service by service id:{} failed.", id);
            deployServiceEntity.setServiceState(ServiceState.STARTING_FAILED);
            deployServiceEntityHandler.storeAndFlush(deployServiceEntity);
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
        ServiceStateManageRequest serviceStateManageRequest =
                getServiceManagerRequest(deployResourceList,
                        deployServiceEntity.getDeployRequest().getServiceHostingType(),
                        deployServiceEntity.getUserId());
        return plugin.startService(serviceStateManageRequest);
    }

    private boolean stop(DeployServiceEntity deployServiceEntity) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(deployServiceEntity.getCsp());
        List<DeployResourceEntity> deployResourceList =
                getVmDeployResourceEntities(deployServiceEntity);
        ServiceStateManageRequest serviceStateManageRequest =
                getServiceManagerRequest(deployResourceList,
                        deployServiceEntity.getDeployRequest().getServiceHostingType(),
                        deployServiceEntity.getUserId());
        return plugin.stopService(serviceStateManageRequest);
    }

    private boolean restart(DeployServiceEntity deployServiceEntity) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(deployServiceEntity.getCsp());
        List<DeployResourceEntity> deployResourceList =
                getVmDeployResourceEntities(deployServiceEntity);
        ServiceStateManageRequest serviceStateManageRequest =
                getServiceManagerRequest(deployResourceList,
                        deployServiceEntity.getDeployRequest().getServiceHostingType(),
                        deployServiceEntity.getUserId());
        return plugin.restartService(serviceStateManageRequest);
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


    private ServiceStateManageRequest getServiceManagerRequest(
            List<DeployResourceEntity> deployResourceList, ServiceHostingType serviceHostingType,
            String userId) {
        ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setDeployResourceEntityList(deployResourceList);
        if (serviceHostingType == ServiceHostingType.SELF) {
            serviceStateManageRequest.setUserId(userId);
        }
        serviceStateManageRequest.setRegionName(
                deployResourceList.get(0).getProperties().get("region"));
        return serviceStateManageRequest;
    }
}
