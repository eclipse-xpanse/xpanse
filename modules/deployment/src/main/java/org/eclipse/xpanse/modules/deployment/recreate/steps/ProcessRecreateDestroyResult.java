/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate.steps;

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
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.recreate.RecreateService;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Processing class for checking destroy status after destroy callback.
 */
@Slf4j
@Component
public class ProcessRecreateDestroyResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final DeployServiceEntityHandler deployServiceEntityHandler;
    private final RecreateService recreateService;

    /**
     * Constructor for ProcessDestroyResult bean.
     */
    @Autowired
    public ProcessRecreateDestroyResult(RuntimeService runtimeService,
                                        DeployServiceEntityHandler deployServiceEntityHandler,
                                        RecreateService recreateService) {
        this.runtimeService = runtimeService;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
        this.recreateService = recreateService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        String serviceId = variables.get(RecreateConstants.ID).toString();

        log.info("Recreate workflow of Instance Id : {} start check destroy status",
                processInstanceId);
        ServiceRecreateEntity serviceRecreateEntity =
                recreateService.getServiceRecreateEntityById(UUID.fromString(processInstanceId));

        boolean isDestroySuccess =
                isDestroySuccess(UUID.fromString(serviceId));

        if (isDestroySuccess) {
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DESTROY_COMPLETED, OffsetDateTime.now());
            runtimeService.setVariable(processInstanceId, RecreateConstants.IS_DESTROY_SUCCESS,
                    true);
            runtimeService.setVariable(processInstanceId, RecreateConstants.DEPLOY_RETRY_NUM, 0);
            log.info("destroy step completed for recreate order {}", processInstanceId);
        } else {
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DESTROY_FAILED, OffsetDateTime.now());

            runtimeService.setVariable(processInstanceId, RecreateConstants.IS_DESTROY_SUCCESS,
                    false);
            int destroyRetryNum = updateDestroyRetryNum(processInstanceId, variables);
            if (destroyRetryNum >= 1) {
                String userId = (String) variables.get(RecreateConstants.USER_ID);
                runtimeService.setVariable(processInstanceId, RecreateConstants.ASSIGNEE, userId);
            }
        }
    }

    private int updateDestroyRetryNum(String processInstanceId, Map<String, Object> variables) {
        int destroyRetryNum = (int) variables.get(RecreateConstants.DESTROY_RETRY_NUM);
        if (destroyRetryNum > 0) {
            log.info("Process instance: {} retry destroy service,RetryNum:{}",
                    processInstanceId, destroyRetryNum);
        }
        runtimeService.setVariable(processInstanceId, RecreateConstants.DESTROY_RETRY_NUM,
                destroyRetryNum + 1);
        return destroyRetryNum;
    }

    private boolean isDestroySuccess(UUID serviceId) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(serviceId);

        if (Objects.isNull(deployServiceEntity)) {
            return false;
        }
        return deployServiceEntity.getServiceDeploymentState()
                == ServiceDeploymentState.DESTROY_SUCCESS;
    }
}