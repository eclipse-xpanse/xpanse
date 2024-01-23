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
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.eclipse.xpanse.modules.models.workflow.migrate.exceptions.ServiceMigrationFailedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process deployment service processing class.
 */
@Slf4j
@Component
public class StartDeploy implements Serializable, JavaDelegate {


    private static DeployService deployService;
    private static RuntimeService runtimeService;
    private static MigrationService migrationService;

    @Autowired
    public void setDeployService(DeployService deployService) {
        StartDeploy.deployService = deployService;
    }

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        StartDeploy.runtimeService = runtimeService;
    }

    @Autowired
    public void setMigrationService(MigrationService migrationService) {
        StartDeploy.migrationService = migrationService;
    }

    /**
     * Methods when performing deployment tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID newServiceId = (UUID) variables.get(MigrateConstants.NEW_ID);
        log.info("Migration workflow of Instance Id : {} start deploy new service with id:{}",
                processInstanceId, newServiceId.toString());

        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityById(UUID.fromString(processInstanceId));
        try {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DEPLOY_STARTED, OffsetDateTime.now());
            startDeploy(processInstanceId, newServiceId, variables);
        } catch (ServiceMigrationFailedException e) {
            log.info("Migration workflow of Instance Id : {} start deploy new service with id: {},"
                    + " error: {}", processInstanceId, newServiceId, e.getMessage());
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DEPLOY_FAILED, OffsetDateTime.now());
        }
    }

    private void startDeploy(String processInstanceId, UUID newServiceId,
            Map<String, Object> variables) {
        runtimeService.updateBusinessKey(processInstanceId, newServiceId.toString());
        String userId = (String) variables.get(MigrateConstants.USER_ID);
        MigrateRequest migrateRequest =
                (MigrateRequest) variables.get(MigrateConstants.MIGRATE_REQUEST);
        DeployRequest deployRequest = new DeployRequest();
        BeanUtils.copyProperties(migrateRequest, deployRequest);
        deployService.deployServiceById(newServiceId, userId, deployRequest);
    }
}
