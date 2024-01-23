/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration.steps;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.eclipse.xpanse.modules.models.workflow.migrate.exceptions.ServiceMigrationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process export data processing class.
 */
@Slf4j
@Component
public class ExportData implements Serializable, JavaDelegate {

    private static RuntimeService runtimeService;
    private static MigrationService migrationService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        ExportData.runtimeService = runtimeService;
    }

    @Autowired
    public void setMigrationService(MigrationService migrationService) {
        ExportData.migrationService = migrationService;
    }

    /**
     * Migration process, export data link business logic(Not yet developed).
     */
    @Override
    public void execute(DelegateExecution delegateExecution) {
        String processInstanceId = delegateExecution.getProcessInstanceId();
        log.info("start export data.ProcessInstanceId:{}", processInstanceId);

        ServiceMigrationEntity entity = saveServiceMigrationEntity(processInstanceId);

        try {
            entity = migrationService.updateServiceMigrationStatus(entity,
                    MigrationStatus.DATA_EXPORT_STARTED, OffsetDateTime.now());

            runtimeService.setVariable(processInstanceId, MigrateConstants.DEPLOY_RETRY_NUM, 0);
            //TODO Export data process not yet implemented. Skipping.

            migrationService.updateServiceMigrationStatus(entity,
                    MigrationStatus.DATA_EXPORT_COMPLETED, OffsetDateTime.now());
        } catch (ServiceMigrationFailedException e) {
            log.error("Migrate Service Export Data error, id:{}, error:{}", processInstanceId,
                    e.getMessage());
            migrationService.updateServiceMigrationStatus(entity,
                    MigrationStatus.DATA_EXPORT_FAILED, OffsetDateTime.now());
        }
    }

    private ServiceMigrationEntity saveServiceMigrationEntity(String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        MigrateRequest migrateRequest =
                (MigrateRequest) variables.get(MigrateConstants.MIGRATE_REQUEST);
        UUID newServiceId = (UUID) variables.get(MigrateConstants.NEW_ID);
        String userId = (String) variables.get(MigrateConstants.USER_ID);
        ServiceMigrationEntity serviceMigrationEntity =
                getServiceMigrationEntity(processInstanceId, migrateRequest.getId(),
                        newServiceId, userId);
        return migrationService.storeOrFlushServiceMigrationEntity(serviceMigrationEntity);
    }


    private ServiceMigrationEntity getServiceMigrationEntity(String processInstanceId,
            UUID oldServiceId, UUID newServiceId, String userId) {
        ServiceMigrationEntity entity = new ServiceMigrationEntity();
        entity.setMigrationId(UUID.fromString(processInstanceId));
        entity.setOldServiceId(oldServiceId);
        entity.setNewServiceId(newServiceId);
        entity.setMigrationStatus(MigrationStatus.MIGRATION_STARTED);
        entity.setUserId(userId);
        entity.setCreateTime(OffsetDateTime.now());
        return entity;
    }
}
