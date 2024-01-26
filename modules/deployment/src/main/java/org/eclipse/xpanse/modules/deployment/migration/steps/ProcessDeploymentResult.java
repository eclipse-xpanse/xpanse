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
 * Processing class for checking deployment status after deployment callback.
 */
@Slf4j
@Component
public class ProcessDeploymentResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final MigrationService migrationService;
    private final DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Constructor for ProcessDeploymentResult bean.
     */
    @Autowired
    public ProcessDeploymentResult(RuntimeService runtimeService, MigrationService migrationService,
                                   DeployServiceEntityHandler deployServiceEntityHandler) {
        this.runtimeService = runtimeService;
        this.migrationService = migrationService;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
    }

    @Override
    public void execute(DelegateExecution execution) {

        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID newServiceId = (UUID) variables.get(MigrateConstants.NEW_ID);
        log.info("Migration workflow of Instance Id : {} check deploy service status with id:{}",
                processInstanceId, newServiceId.toString());

        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityById(UUID.fromString(processInstanceId));
        boolean isDeploySuccess = isDeploySuccess(newServiceId);
        if (isDeploySuccess) {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DEPLOY_COMPLETED, OffsetDateTime.now());
            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DEPLOY_SUCCESS,
                    true);
            log.info("deployment step completed for migration order {}", processInstanceId);
        } else {
            migrationService.updateServiceMigrationStatus(serviceMigrationEntity,
                    MigrationStatus.DEPLOY_FAILED, OffsetDateTime.now());
            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DEPLOY_SUCCESS,
                    false);
            int deployRetryNum = updateDeployRetryNum(processInstanceId, variables);
            if (deployRetryNum >= 1) {
                String userId = (String) variables.get(MigrateConstants.USER_ID);
                runtimeService.setVariable(processInstanceId, MigrateConstants.ASSIGNEE,
                        userId);
            }
        }
    }

    private int updateDeployRetryNum(String processInstanceId, Map<String, Object> variables) {
        int deployRetryNum = (int) variables.get(MigrateConstants.DEPLOY_RETRY_NUM);
        if (deployRetryNum > 0) {
            log.info("Process instance: {} retry deployment service, RetryCount:{}",
                    processInstanceId, deployRetryNum);
        }
        runtimeService.setVariable(processInstanceId, MigrateConstants.DEPLOY_RETRY_NUM,
                deployRetryNum + 1);
        return deployRetryNum;
    }

    private boolean isDeploySuccess(UUID newServiceId) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(newServiceId);

        if (Objects.isNull(deployServiceEntity)) {
            return false;
        }
        return deployServiceEntity.getServiceDeploymentState()
                == ServiceDeploymentState.DEPLOY_SUCCESS;
    }
}
