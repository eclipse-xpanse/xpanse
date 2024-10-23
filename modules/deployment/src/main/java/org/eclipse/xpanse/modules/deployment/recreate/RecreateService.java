/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateEntity;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateQueryModel;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceRecreateOrderNotFoundException;
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;
import org.eclipse.xpanse.modules.models.workflow.recreate.view.ServiceRecreateDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Service class for service recreate.
 */
@Slf4j
@Service
public class RecreateService {

    @Resource
    private ServiceRecreateStorage serviceRecreateStorage;

    /**
     * Save or refresh ServiceRecreateEntity.
     */
    public ServiceRecreateEntity storeOrFlushServiceRecreateEntity(
            ServiceRecreateEntity serviceRecreateEntity) {
        return serviceRecreateStorage.storeAndFlush(serviceRecreateEntity);
    }

    /**
     * Get ServiceRecreateEntity based on recreate id.
     *
     * @param id Recreate Id.
     * @return ServiceRecreateEntity.
     */
    public ServiceRecreateEntity getServiceRecreateEntityById(UUID id) {
        return serviceRecreateStorage.findServiceRecreateById(id);
    }

    /**
     * Update the status of ServiceRecreateEntity.
     */
    public ServiceRecreateEntity updateServiceRecreateStatus(
            ServiceRecreateEntity serviceRecreateEntity, RecreateStatus status,
            OffsetDateTime time) {
        serviceRecreateEntity.setRecreateStatus(status);
        serviceRecreateEntity.setLastModifiedTime(time);
        return storeOrFlushServiceRecreateEntity(serviceRecreateEntity);
    }

    /**
     * Get ServiceRecreateEntity based on newServiceId.
     *
     * @param taskId newServiceId.
     * @return ServiceRecreateEntity.
     */
    public ServiceRecreateEntity getServiceRecreateEntityByNewServiceId(UUID taskId) {
        ServiceRecreateQueryModel query = new ServiceRecreateQueryModel();
        List<ServiceRecreateEntity> serviceRecreateEntities =
                serviceRecreateStorage.listServiceRecreates(query);
        if (CollectionUtils.isEmpty(serviceRecreateEntities)) {
            return null;
        }
        return serviceRecreateEntities.getFirst();
    }


    /**
     * Get ServiceRecreateEntity based on serviceId.
     *
     * @param taskId oldServiceId.
     * @return ServiceRecreateEntity.
     */
    public ServiceRecreateEntity getServiceRecreateEntityByOldServiceId(UUID taskId) {
        ServiceRecreateQueryModel query = new ServiceRecreateQueryModel();
        query.setServiceId(taskId);
        List<ServiceRecreateEntity> serviceRecreateEntities =
                serviceRecreateStorage.listServiceRecreates(query);
        if (CollectionUtils.isEmpty(serviceRecreateEntities)) {
            return null;
        }
        return serviceRecreateEntities.getFirst();
    }

    /**
     * Get recreate records based on recreate id.
     *
     * @param recreateId ID of the service recreate.
     * @return serviceRecreateEntity.
     */
    public ServiceRecreateDetails getRecreateOrderDetails(UUID recreateId, String userId) {
        ServiceRecreateEntity serviceRecreate =
                serviceRecreateStorage.findServiceRecreateById(recreateId);
        if (Objects.isNull(serviceRecreate)) {
            String errorMsg = String.format("Service recreate with id %s not found.", recreateId);
            throw new ServiceRecreateOrderNotFoundException(errorMsg);
        }

        if (!StringUtils.equals(userId, serviceRecreate.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view service recreate belonging to other users.");
        }
        return EntityTransUtils.transToServiceRecreateDetails(serviceRecreate);
    }

    /**
     * List all services recreate by a user.
     *
     * @param recreateId     ID of the service recreate.
     * @param serviceId      ID of the service.
     * @param recreateStatus Status of the service recreate.
     * @return list of all services deployed by a user.
     */
    public List<ServiceRecreateDetails> listServiceRecreates(UUID recreateId,
                                                             UUID serviceId,
                                                             RecreateStatus recreateStatus,
                                                             String userId) {
        ServiceRecreateQueryModel queryModel = new ServiceRecreateQueryModel();
        queryModel.setRecreateId(recreateId);
        queryModel.setServiceId(serviceId);
        queryModel.setRecreateStatus(recreateStatus);
        queryModel.setUserId(userId);

        List<ServiceRecreateEntity> serviceRecreateEntities =
                serviceRecreateStorage.listServiceRecreates(queryModel);
        List<ServiceRecreateDetails> serviceRecreateDetailsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serviceRecreateEntities)) {
            for (ServiceRecreateEntity serviceRecreateEntity : serviceRecreateEntities) {
                ServiceRecreateDetails details =
                        EntityTransUtils.transToServiceRecreateDetails(serviceRecreateEntity);
                serviceRecreateDetailsList.add(details);
            }
        }
        return serviceRecreateDetailsList;
    }
}
