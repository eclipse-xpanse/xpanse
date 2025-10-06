/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.InvalidServiceDeploymentStateException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.springframework.stereotype.Component;

/** Bean for grouping common methods for handling ServiceDeploymentEntity. */
@Slf4j
@Component
public class ServiceDeploymentEntityHandler {

    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;

    /**
     * Get deploy service entity by id.
     *
     * @param id service id.
     * @return deploy service entity.
     */
    public ServiceDeploymentEntity getServiceDeploymentEntity(UUID id) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentStorage.findServiceDeploymentById(id);
        if (Objects.isNull(serviceDeploymentEntity)) {
            String errorMsg = String.format("Service with id %s not found.", id);
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        return serviceDeploymentEntity;
    }

    /** Validate service in the deployment state is allowed to perform the order type. */
    public void validateServiceDeploymentStateForOrderType(
            ServiceDeploymentEntity serviceDeploymentEntity, ServiceOrderType serviceOrderType) {
        if (!isValidOrderRequestForDeployment(
                serviceDeploymentEntity.getServiceDeploymentState(), serviceOrderType)) {
            String errorMsg =
                    String.format(
                            "Request with type %s to the service %s with the deployment state %s is"
                                    + " not allowed.",
                            serviceOrderType.toValue(),
                            serviceDeploymentEntity.getId(),
                            serviceDeploymentEntity.getServiceDeploymentState().toValue());
            log.error(errorMsg);
            throw new InvalidServiceDeploymentStateException(errorMsg);
        }
    }

    private boolean isValidOrderRequestForDeployment(
            ServiceDeploymentState state, ServiceOrderType serviceOrderType) {
        if (state == ServiceDeploymentState.DEPLOYING
                || state == ServiceDeploymentState.DESTROYING
                || state == ServiceDeploymentState.MODIFYING) {
            return false;
        }
        return switch (serviceOrderType) {
            case MODIFY,
                    PORT,
                    DESTROY,
                    LOCK_CHANGE,
                    CONFIG_CHANGE,
                    SERVICE_ACTION,
                    SERVICE_START,
                    SERVICE_STOP,
                    SERVICE_RESTART,
                    OBJECT_CREATE,
                    OBJECT_MODIFY,
                    OBJECT_DELETE ->
                    state == ServiceDeploymentState.DEPLOY_SUCCESS
                            || state == ServiceDeploymentState.MODIFICATION_SUCCESSFUL
                            || state == ServiceDeploymentState.MODIFICATION_FAILED;
            case RECREATE ->
                    state == ServiceDeploymentState.DEPLOY_SUCCESS
                            || state == ServiceDeploymentState.DESTROY_SUCCESS
                            || state == ServiceDeploymentState.MODIFICATION_SUCCESSFUL
                            || state == ServiceDeploymentState.MODIFICATION_FAILED;
            case RETRY ->
                    state == ServiceDeploymentState.DEPLOY_FAILED
                            || state == ServiceDeploymentState.ROLLBACK_FAILED;
            case PURGE ->
                    state == ServiceDeploymentState.DESTROY_SUCCESS
                            || state == ServiceDeploymentState.DEPLOY_FAILED
                            || state == ServiceDeploymentState.ROLLBACK_FAILED;
            case ROLLBACK -> state == ServiceDeploymentState.DEPLOY_FAILED;
            default -> true;
        };
    }

    /**
     * Store and flush service deployment entity.
     *
     * @param serviceDeploymentEntity service deployment entity.
     * @return updated service deployment entity.
     */
    public ServiceDeploymentEntity storeAndFlush(ServiceDeploymentEntity serviceDeploymentEntity) {
        return serviceDeploymentStorage.storeAndFlush(serviceDeploymentEntity);
    }

    /**
     * Update service deployment status.
     *
     * @param serviceDeployment service deployment entity.
     * @param state deployment status
     */
    public void updateServiceDeploymentStatus(
            ServiceDeploymentEntity serviceDeployment, ServiceDeploymentState state) {
        serviceDeployment.setServiceDeploymentState(state);
        serviceDeploymentStorage.storeAndFlush(serviceDeployment);
    }
}
