/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration.steps;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Processing class for checking destroy status after destroy callback.
 */
@Slf4j
@Component
public class ProcessDestroyResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final DeployServiceEntityHandler deployServiceEntityHandler;
    private final MigrationService migrationService;

    /**
     * Constructor for ProcessDestroyResult bean.
     */
    @Autowired
    public ProcessDestroyResult(RuntimeService runtimeService,
                                DeployServiceEntityHandler deployServiceEntityHandler,
                                MigrationService migrationService) {
        this.runtimeService = runtimeService;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
        this.migrationService = migrationService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        String oldServiceId = variables.get(MigrateConstants.ID).toString();

        log.info("Migration workflow of Instance Id : {} start check destroy status",
                processInstanceId);
        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityById(UUID.fromString(processInstanceId));

        boolean isDestroySuccess =
                isDestroySuccess(UUID.fromString(oldServiceId));

        if (isDestroySuccess) {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DESTROY_COMPLETED, OffsetDateTime.now());
            String newServiceId = variables.get(MigrateConstants.NEW_ID).toString();
            updateStatus(serviceMigrationEntity, UUID.fromString(newServiceId),
                    UUID.fromString(oldServiceId));
            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DESTROY_SUCCESS,
                    true);
            log.info("destroy step completed for migration order {}", processInstanceId);
        } else {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DESTROY_FAILED, OffsetDateTime.now());

            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DESTROY_SUCCESS,
                    false);
            int destroyRetryNum = updateDestroyRetryNum(processInstanceId, variables);
            if (destroyRetryNum >= 1) {
                String userId = (String) variables.get(MigrateConstants.USER_ID);
                runtimeService.setVariable(processInstanceId, MigrateConstants.ASSIGNEE, userId);
            }
        }
    }

    private int updateDestroyRetryNum(String processInstanceId, Map<String, Object> variables) {
        int destroyRetryNum = (int) variables.get(MigrateConstants.DESTROY_RETRY_NUM);
        if (destroyRetryNum > 0) {
            log.info("Process instance: {} retry destroy service,RetryNum:{}",
                    processInstanceId, destroyRetryNum);
        }
        runtimeService.setVariable(processInstanceId, MigrateConstants.DESTROY_RETRY_NUM,
                destroyRetryNum + 1);
        return destroyRetryNum;
    }

    private boolean isDestroySuccess(UUID oldServiceId) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(oldServiceId);

        if (Objects.isNull(deployServiceEntity)) {
            return false;
        }
        return deployServiceEntity.getServiceDeploymentState()
                == ServiceDeploymentState.DESTROY_SUCCESS;
    }

    private boolean isMigrationSuccess(UUID newServiceId, UUID oldServiceId) {

        DeployServiceEntity newDeployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(newServiceId);

        DeployServiceEntity oldDeployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(oldServiceId);

        if (Objects.nonNull(newDeployServiceEntity) && Objects.nonNull(oldDeployServiceEntity)) {
            return newDeployServiceEntity.getServiceDeploymentState()
                    == ServiceDeploymentState.DEPLOY_SUCCESS
                    && oldDeployServiceEntity.getServiceDeploymentState()
                    == ServiceDeploymentState.DESTROY_SUCCESS;
        }
        return false;
    }

    private void updateStatus(ServiceMigrationEntity serviceMigrationEntity, UUID newServiceId,
            UUID oldServiceId) {
        if (isMigrationSuccess(newServiceId, oldServiceId)) {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.MIGRATION_COMPLETED, OffsetDateTime.now());
        } else {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.MIGRATION_FAILED, OffsetDateTime.now());
        }
    }
}