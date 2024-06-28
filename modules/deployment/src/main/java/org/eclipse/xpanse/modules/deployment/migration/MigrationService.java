/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationQueryModel;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceMigrationNotFoundException;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.eclipse.xpanse.modules.models.workflow.migrate.view.ServiceMigrationDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Service class for service migration.
 */
@Slf4j
@Service
public class MigrationService {

    @Resource
    private ServiceMigrationStorage serviceMigrationStorage;
    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Save or refresh ServiceMigrationEntity.
     */
    public ServiceMigrationEntity storeOrFlushServiceMigrationEntity(
            ServiceMigrationEntity serviceMigrationEntity) {
        return serviceMigrationStorage.storeAndFlush(serviceMigrationEntity);
    }

    /**
     * Get ServiceMigrationEntity based on migration id.
     *
     * @param id Migration Id.
     * @return ServiceMigrationEntity.
     */
    public ServiceMigrationEntity getServiceMigrationEntityById(UUID id) {
        return serviceMigrationStorage.findServiceMigrationById(id);
    }

    /**
     * Update the status of ServiceMigrationEntity.
     */
    public ServiceMigrationEntity updateServiceMigrationStatus(
            ServiceMigrationEntity serviceMigrationEntity, MigrationStatus status,
            OffsetDateTime time) {
        serviceMigrationEntity.setMigrationStatus(status);
        serviceMigrationEntity.setLastModifiedTime(time);
        return storeOrFlushServiceMigrationEntity(serviceMigrationEntity);
    }

    /**
     * Get ServiceMigrationEntity based on newServiceId.
     *
     * @param taskId newServiceId.
     * @return ServiceMigrationEntity.
     */
    public ServiceMigrationEntity getServiceMigrationEntityByNewServiceId(UUID taskId) {
        ServiceMigrationQueryModel query = new ServiceMigrationQueryModel();
        query.setNewServiceId(taskId);
        List<ServiceMigrationEntity> serviceMigrationEntities =
                serviceMigrationStorage.listServiceMigrations(query);
        if (CollectionUtils.isEmpty(serviceMigrationEntities)) {
            return null;
        }
        return serviceMigrationEntities.getFirst();
    }


    /**
     * Get ServiceMigrationEntity based on oldServiceId.
     *
     * @param taskId oldServiceId.
     * @return ServiceMigrationEntity.
     */
    public ServiceMigrationEntity getServiceMigrationEntityByOldServiceId(UUID taskId) {
        ServiceMigrationQueryModel query = new ServiceMigrationQueryModel();
        query.setOldServiceId(taskId);
        List<ServiceMigrationEntity> serviceMigrationEntities =
                serviceMigrationStorage.listServiceMigrations(query);
        if (CollectionUtils.isEmpty(serviceMigrationEntities)) {
            return null;
        }
        return serviceMigrationEntities.getFirst();
    }

    /**
     * Get migration records based on migration id.
     *
     * @param migrationId ID of the service migrate.
     * @return serviceMigrationEntity.
     */
    public ServiceMigrationDetails getMigrationOrderDetails(UUID migrationId, String userId) {
        ServiceMigrationEntity serviceMigration =
                serviceMigrationStorage.findServiceMigrationById(migrationId);
        if (Objects.isNull(serviceMigration)) {
            String errorMsg = String.format("Service migration with id %s not found.", migrationId);
            throw new ServiceMigrationNotFoundException(errorMsg);
        }

        if (!StringUtils.equals(userId, serviceMigration.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view service migration belonging to other users.");
        }
        return EntityTransUtils.transToServiceMigrationDetails(serviceMigration);
    }

    /**
     * List all services migration by a user.
     *
     * @param migrationId     ID of the service migrate.
     * @param newServiceId    ID of the new service.
     * @param oldServiceId    ID of the old service.
     * @param migrationStatus Status of the service migrate.
     * @return list of all services deployed by a user.
     */
    public List<ServiceMigrationDetails> listServiceMigrations(UUID migrationId, UUID newServiceId,
                                                               UUID oldServiceId,
                                                               MigrationStatus migrationStatus,
                                                               String userId) {
        ServiceMigrationQueryModel queryModel = new ServiceMigrationQueryModel();
        queryModel.setMigrationId(migrationId);
        queryModel.setNewServiceId(newServiceId);
        queryModel.setOldServiceId(oldServiceId);
        queryModel.setMigrationStatus(migrationStatus);
        queryModel.setUserId(userId);

        List<ServiceMigrationEntity> serviceMigrationEntities =
                serviceMigrationStorage.listServiceMigrations(queryModel);
        List<ServiceMigrationDetails> serviceMigrationDetailsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serviceMigrationEntities)) {
            for (ServiceMigrationEntity serviceMigrationEntity : serviceMigrationEntities) {
                ServiceMigrationDetails details =
                        EntityTransUtils.transToServiceMigrationDetails(serviceMigrationEntity);
                serviceMigrationDetailsList.add(details);
            }
        }
        return serviceMigrationDetailsList;
    }

    /**
     * Query service template.
     */
    public ServiceTemplateEntity findServiceTemplate(ServiceTemplateEntity searchServiceTemplate) {
        return serviceTemplateStorage.findServiceTemplate(searchServiceTemplate);
    }
}
