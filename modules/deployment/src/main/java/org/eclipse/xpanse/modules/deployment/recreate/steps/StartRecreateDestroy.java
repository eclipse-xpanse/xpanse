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
public class StartRecreateDestroy implements Serializable, JavaDelegate {


    @Serial
    private static final long serialVersionUID = 2725212494105579585L;

    private final DeployService deployService;
    private final RuntimeService runtimeService;
    private final RecreateService recreateService;

    /**
     * Constructor for StartRecreateDeploy bean.
     */
    @Autowired
    public StartRecreateDestroy(DeployService deployService, RuntimeService runtimeService,
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
        ServiceRecreateEntity serviceRecreateEntity = saveServiceRecreateEntity(processInstanceId);

        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID serviceId = (UUID) variables.get(RecreateConstants.ID);
        log.info("Recreate workflow of Instance Id : {} start destroy old service with id:{}",
                processInstanceId, serviceId);

        try {
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DESTROY_STARTED, OffsetDateTime.now());

            if (!variables.containsKey(RecreateConstants.DESTROY_RETRY_NUM)) {
                runtimeService.setVariable(processInstanceId, RecreateConstants.DESTROY_RETRY_NUM,
                        0);
            }

            deployService.destroyServiceByWorkflow(serviceId, processInstanceId, null);
        } catch (ServiceRecreateFailedException e) {
            log.info("Recreate workflow of Instance Id : {} start destroy old service with id: {},"
                    + " error: {}", processInstanceId, serviceId, e.getMessage());
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DESTROY_FAILED, OffsetDateTime.now());
        }

    }

    private ServiceRecreateEntity saveServiceRecreateEntity(String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        DeployRequest deployRequest =
                (DeployRequest) variables.get(RecreateConstants.RECREATE_REQUEST);
        String userId = (String) variables.get(RecreateConstants.USER_ID);
        ServiceRecreateEntity serviceRecreateEntity =
                getServiceRecreateEntity(processInstanceId, deployRequest.getServiceId(),
                        userId);
        return recreateService.storeOrFlushServiceRecreateEntity(serviceRecreateEntity);
    }


    private ServiceRecreateEntity getServiceRecreateEntity(String processInstanceId,
                                                           UUID serviceId, String userId) {
        ServiceRecreateEntity entity = new ServiceRecreateEntity();
        entity.setRecreateId(UUID.fromString(processInstanceId));
        entity.setServiceId(serviceId);
        entity.setRecreateStatus(RecreateStatus.RECREATE_STARTED);
        entity.setUserId(userId);
        entity.setCreateTime(OffsetDateTime.now());
        return entity;
    }
}

