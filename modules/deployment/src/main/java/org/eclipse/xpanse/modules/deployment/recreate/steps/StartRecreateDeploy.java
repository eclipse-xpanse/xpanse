/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate.steps;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.recreate.RecreateService;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;
import org.eclipse.xpanse.modules.models.workflow.recreate.exceptions.ServiceRecreateFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Recreate process deployment service processing class.
 */
@Slf4j
@Component
public class StartRecreateDeploy implements Serializable, JavaDelegate {


    @Serial
    private static final long serialVersionUID = 2725212494105579585L;

    private final DeployService deployService;
    private final RuntimeService runtimeService;
    private final RecreateService recreateService;

    /**
     * Constructor for StartRecreateDeploy bean.
     */
    @Autowired
    public StartRecreateDeploy(DeployService deployService, RuntimeService runtimeService,
                               RecreateService recreateService) {
        this.deployService = deployService;
        this.runtimeService = runtimeService;
        this.recreateService = recreateService;
    }

    /**
     * Methods when performing deployment tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID originalServiceId = (UUID) variables.get(RecreateConstants.ID);
        log.info("Recreate workflow of Instance Id : {} start deploy new service with id:{}",
                processInstanceId, originalServiceId.toString());

        ServiceRecreateEntity serviceRecreateEntity =
                recreateService.getServiceRecreateEntityById(UUID.fromString(processInstanceId));
        try {
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DEPLOY_STARTED, OffsetDateTime.now());
            startDeploy(processInstanceId, originalServiceId, variables);
        } catch (ServiceRecreateFailedException e) {
            log.info("Recreate workflow of Instance Id : {} start deploy new service with id: {},"
                    + " error: {}", processInstanceId, originalServiceId, e.getMessage());
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DEPLOY_FAILED, OffsetDateTime.now());
        }
    }

    private void startDeploy(String processInstanceId, UUID originalServiceId,
                             Map<String, Object> variables) {
        runtimeService.updateBusinessKey(processInstanceId, originalServiceId.toString());
        String userId = (String) variables.get(RecreateConstants.USER_ID);
        DeployRequest deployRequest =
                (DeployRequest) variables.get(RecreateConstants.RECREATE_REQUEST);
        deployRequest.setUserId(userId);
        deployService.deployServiceByWorkflow(originalServiceId, processInstanceId,
                UUID.randomUUID(), deployRequest);
    }
}

