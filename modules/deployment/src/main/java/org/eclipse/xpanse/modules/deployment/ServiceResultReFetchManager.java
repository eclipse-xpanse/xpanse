/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.TofuMakerResultReFetchManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.TerraBootResultReFetchManager;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage all methods used by the service operation result re-fetch manager. */
@Slf4j
@Component
public class ServiceResultReFetchManager {

    @Value("${max.service.order.processing.duration.in.seconds}")
    private int maxServiceOrderProcessingDuration;

    @Resource private TerraBootResultReFetchManager terraBootResultRefetchManager;
    @Resource private TofuMakerResultReFetchManager tofuMakerResultRefetchManager;

    /** ReFetch deploymentState for missing service orders. */
    public void reFetchDeploymentStateForMissingOrdersFromDeployers(
            ServiceDeploymentEntity serviceDeployment) {
        try {
            ServiceOrderEntity serviceOrderEntity =
                    getServiceOrderEntityForDeployedService(serviceDeployment);
            if (Objects.isNull(serviceOrderEntity)) {
                return;
            }
            DeployerKind deployerKind =
                    getDeployerKind(serviceOrderEntity.getServiceDeploymentEntity());
            if (waitTimeExceedMaxServiceOrderProcessingDuration(serviceOrderEntity)) {
                if (DeployerKind.TERRAFORM == deployerKind) {
                    terraBootResultRefetchManager.retrieveTerraformResult(serviceOrderEntity);
                }
                if (DeployerKind.OPEN_TOFU == deployerKind) {
                    tofuMakerResultRefetchManager.retrieveOpenTofuResult(serviceOrderEntity);
                }
            }
        } catch (ServiceOrderNotFound e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Batch reFetch deploymentState for missing service orders.
     *
     * @param serviceDeploymentEntities serviceDeploymentEntities.
     */
    public void batchReFetchDeploymentStateForMissingOrdersFromDeployers(
            List<ServiceDeploymentEntity> serviceDeploymentEntities) {
        List<ServiceOrderEntity> serviceOrders = new ArrayList<>();
        serviceDeploymentEntities.forEach(
                serviceDeploymentEntity -> {
                    try {
                        ServiceOrderEntity serviceOrderEntity =
                                getServiceOrderEntityForDeployedService(serviceDeploymentEntity);
                        if (Objects.nonNull(serviceOrderEntity)) {
                            serviceOrders.add(serviceOrderEntity);
                        }
                    } catch (ServiceOrderNotFound e) {
                        log.error(e.getMessage());
                    }
                });
        batchFeFetchDeploymentStateForMissingOrdersFromDeployers(serviceOrders);
    }

    private ServiceOrderEntity getServiceOrderEntityForDeployedService(
            ServiceDeploymentEntity serviceDeployment) {
        List<ServiceOrderEntity> serviceOrderEntities = serviceDeployment.getServiceOrders();
        ServiceOrderEntity serviceOrderEntity = null;
        if (serviceDeployment
                .getServiceDeploymentState()
                .equals(ServiceDeploymentState.DEPLOYING)) {
            serviceOrderEntity =
                    serviceOrderEntities.stream()
                            .filter(
                                    serviceOrder ->
                                            serviceOrder.getTaskType() == ServiceOrderType.DEPLOY
                                                    && serviceOrder.getOrderStatus()
                                                            == OrderStatus.IN_PROGRESS)
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new ServiceOrderNotFound(
                                                    String.format(
                                                            "No ServiceOrderEntity found with"
                                                                + " serviceId %s, ServiceOrderType"
                                                                + " DEPLOY",
                                                            serviceDeployment.getId())));
        } else if (serviceDeployment
                .getServiceDeploymentState()
                .equals(ServiceDeploymentState.DESTROYING)) {
            serviceOrderEntity =
                    serviceOrderEntities.stream()
                            .filter(
                                    serviceOrder ->
                                            serviceOrder.getTaskType() == ServiceOrderType.DESTROY
                                                    && serviceOrder.getOrderStatus()
                                                            == OrderStatus.IN_PROGRESS)
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new ServiceOrderNotFound(
                                                    String.format(
                                                            "No ServiceOrderEntity found with"
                                                                + " serviceId %s, ServiceOrderType"
                                                                + " DESTROY",
                                                            serviceDeployment.getId())));
        } else if (serviceDeployment
                .getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFYING)) {
            serviceOrderEntity =
                    serviceOrderEntities.stream()
                            .filter(
                                    serviceOrder ->
                                            serviceOrder.getTaskType() == ServiceOrderType.MODIFY
                                                    && serviceOrder.getOrderStatus()
                                                            == OrderStatus.IN_PROGRESS)
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new ServiceOrderNotFound(
                                                    String.format(
                                                            "No ServiceOrderEntity found with"
                                                                + " serviceId %s, ServiceOrderType"
                                                                + " MODIFY",
                                                            serviceDeployment.getId())));
        }
        return serviceOrderEntity;
    }

    private DeployerKind getDeployerKind(ServiceDeploymentEntity serviceDeployment) {
        ServiceTemplateEntity serviceTemplate = serviceDeployment.getServiceTemplateEntity();
        return serviceTemplate.getOcl().getDeployment().getDeployerTool().getKind();
    }

    private boolean waitTimeExceedMaxServiceOrderProcessingDuration(
            ServiceOrderEntity serviceOrder) {
        return Duration.between(serviceOrder.getStartedTime(), OffsetDateTime.now()).getSeconds()
                > maxServiceOrderProcessingDuration;
    }

    private void batchFeFetchDeploymentStateForMissingOrdersFromDeployers(
            List<ServiceOrderEntity> serviceOrders) {
        if (CollectionUtils.isEmpty(serviceOrders)) {
            return;
        }
        List<ServiceOrderEntity> terraformServiceOrders = new ArrayList<>();
        List<ServiceOrderEntity> tofuServiceOrders = new ArrayList<>();
        for (ServiceOrderEntity serviceOrder : serviceOrders) {
            if (waitTimeExceedMaxServiceOrderProcessingDuration(serviceOrder)) {
                DeployerKind deployerKind =
                        getDeployerKind(serviceOrder.getServiceDeploymentEntity());
                if (DeployerKind.TERRAFORM == deployerKind) {
                    terraformServiceOrders.add(serviceOrder);
                } else if (DeployerKind.OPEN_TOFU == deployerKind) {
                    tofuServiceOrders.add(serviceOrder);
                }
            }
        }
        if (!CollectionUtils.isEmpty(terraformServiceOrders)) {
            terraBootResultRefetchManager.batchRetrieveTerraformResults(terraformServiceOrders);
        }
        if (!CollectionUtils.isEmpty(tofuServiceOrders)) {
            tofuMakerResultRefetchManager.batchRetrieveOpenTofuResults(tofuServiceOrders);
        }
    }
}
