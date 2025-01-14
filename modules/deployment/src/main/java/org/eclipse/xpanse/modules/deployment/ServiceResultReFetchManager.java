/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.TofuMakerResultRefetchManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.TerraformBootResultRefetchManager;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Bean to manage all methods used by the service operation result recapture manager. */
@Slf4j
@Component
public class ServiceResultReFetchManager {

    @Value("${max.service.order.processing.duration.in.seconds}")
    private int maxServiceOrderProcessingDuration;

    @Resource private TerraformBootResultRefetchManager terraformBootResultRefetchManager;
    @Resource private TofuMakerResultRefetchManager tofuMakerResultRefetchManager;
    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    /** ReFetch deploymentState for missing service orders. */
    public void reFetchDeploymentStateForMissingOrdersFromDeployers(
            ServiceDeploymentEntity serviceDeployment) {
        ServiceOrderEntity serviceOrderEntity =
                getServiceOrderEntityForDeployedService(serviceDeployment);
        refetchDeploymentStateForMissingOrdersFromDeployers(serviceDeployment, serviceOrderEntity);
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
                                            serviceOrder
                                                    .getTaskType()
                                                    .equals(ServiceOrderType.DEPLOY))
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
                                            serviceOrder
                                                    .getTaskType()
                                                    .equals(ServiceOrderType.DESTROY))
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
                                            serviceOrder
                                                    .getTaskType()
                                                    .equals(ServiceOrderType.MODIFY))
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

    private void refetchDeploymentStateForMissingOrdersFromDeployers(
            ServiceDeploymentEntity serviceDeployment, ServiceOrderEntity serviceOrder) {
        if (Objects.nonNull(serviceOrder)) {
            ServiceTemplateEntity serviceTemplate =
                    serviceTemplateStorage.getServiceTemplateById(
                            serviceDeployment.getServiceTemplateId());
            DeployerKind deployerKind =
                    serviceTemplate.getOcl().getDeployment().getDeployerTool().getKind();
            if (Duration.between(serviceOrder.getStartedTime(), OffsetDateTime.now()).getSeconds()
                    > maxServiceOrderProcessingDuration) {
                if (DeployerKind.TERRAFORM == deployerKind) {
                    terraformBootResultRefetchManager.retrieveTerraformResult(
                            serviceDeployment, serviceOrder);
                }
                if (DeployerKind.OPEN_TOFU == deployerKind) {
                    tofuMakerResultRefetchManager.retrieveOpenTofuResult(
                            serviceDeployment, serviceOrder);
                }
            }
        }
    }
}
