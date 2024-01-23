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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process destroy service processing class.
 */
@Slf4j
@Component
public class StartDestroy implements Serializable, JavaDelegate {

    private static DeployService deployService;
    private static RuntimeService runtimeService;
    private static MigrationService migrationService;

    @Autowired
    public void setDeployService(DeployService deployService) {
        StartDestroy.deployService = deployService;
    }

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        StartDestroy.runtimeService = runtimeService;
    }

    @Autowired
    public void setMigrationService(MigrationService migrationService) {
        StartDestroy.migrationService = migrationService;
    }

    /**
     * Methods when performing destroy tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        String oldServiceId = variables.get(MigrateConstants.ID).toString();

        log.info("Migration workflow of Instance Id : {} start destroy old service with id:{}",
                processInstanceId, oldServiceId);

        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityById(UUID.fromString(processInstanceId));

        try {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DESTROY_STARTED, OffsetDateTime.now());
            deployService.destroyServiceById(oldServiceId);

        } catch (DeployerNotFoundException e) {
            log.info("Migration workflow of Instance Id: {} start destroy old service with id: {}"
                    + " error: {}", processInstanceId, oldServiceId, e.getMessage());
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DESTROY_FAILED, OffsetDateTime.now());
        }
    }
}
