/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration.steps;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.eclipse.xpanse.modules.models.workflow.migrate.exceptions.ServiceMigrationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process import data processing class.
 */
@Slf4j
@Component
public class ImportData implements Serializable, JavaDelegate {

    private static RuntimeService runtimeService;

    private static MigrationService migrationService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        ImportData.runtimeService = runtimeService;
    }

    @Autowired
    public void setMigrationService(MigrationService migrationService) {
        ImportData.migrationService = migrationService;
    }

    /**
     * Migration process, import data link business logic(Not yet developed).
     */
    @Override
    public void execute(DelegateExecution delegateExecution) {
        String processInstanceId = delegateExecution.getProcessInstanceId();
        log.info("start import data.ProcessInstanceId:{}", processInstanceId);
        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityById(UUID.fromString(processInstanceId));
        try {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DATA_IMPORT_STARTED, OffsetDateTime.now());

            runtimeService.setVariable(processInstanceId, MigrateConstants.DESTROY_RETRY_NUM, 0);
            //TODO Export data process not yet implemented. Skipping.

            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DATA_IMPORT_COMPLETED, OffsetDateTime.now());
        } catch (ServiceMigrationFailedException e) {
            log.error("Migrate Service Import Data error, id:{}, error:{}", processInstanceId,
                    e.getMessage());
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DATA_IMPORT_FAILED, OffsetDateTime.now());
        }

    }
}
