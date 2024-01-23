/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationStorage;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
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
    public ServiceMigrationEntity getServiceMigrationEntityByNewServiceId(String taskId) {
        List<ServiceMigrationEntity> serviceMigrationEntities =
                serviceMigrationStorage.serviceMigrations().stream()
                        .filter(serviceMigrationEntity -> taskId.equals(
                                serviceMigrationEntity.getNewServiceId().toString())).toList();
        if (CollectionUtils.isEmpty(serviceMigrationEntities)) {
            return null;
        }
        return serviceMigrationEntities.get(0);
    }


    /**
     * Get ServiceMigrationEntity based on oldServiceId.
     *
     * @param taskId oldServiceId.
     * @return ServiceMigrationEntity.
     */
    public ServiceMigrationEntity getServiceMigrationEntityByOldServiceId(String taskId) {
        List<ServiceMigrationEntity> serviceMigrationEntities =
                serviceMigrationStorage.serviceMigrations().stream()
                        .filter(serviceMigrationEntity -> taskId.equals(
                                serviceMigrationEntity.getOldServiceId().toString())).toList();
        if (CollectionUtils.isEmpty(serviceMigrationEntities)) {
            return null;
        }
        return serviceMigrationEntities.get(0);
    }
}
