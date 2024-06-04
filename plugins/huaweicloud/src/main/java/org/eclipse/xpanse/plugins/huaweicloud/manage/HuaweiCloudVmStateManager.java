/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.manage;


import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy.WAITING_JOB_SUCCESS_RETRY_TIMES;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobRequest;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse.StatusEnum;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates all Manager-related public methods of the Huawei Cloud plugin.
 */
@Slf4j
@Component
public class HuaweiCloudVmStateManager {
    @Resource
    private CredentialCenter credentialCenter;
    @Resource
    private HuaweiCloudClient huaweiCloudClient;
    @Resource
    private HuaweiCloudServerManageRequestConverter converter;
    @Resource
    private HuaweiCloudRetryStrategy huaweiCloudRetryStrategy;

    /**
     * Start the Huawei Cloud Ecs server.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        try {
            EcsClient ecsClient = getEcsClient(serviceStateManageRequest);
            BatchStartServersRequest request = converter.buildBatchStartServersRequest(
                    serviceStateManageRequest.getDeployResourceEntityList());
            BatchStartServersResponse response = ecsClient.batchStartServersInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            return checkEcsExecResultByJobId(ecsClient, response.getJobId());
        } catch (Exception e) {
            String errorMsg = String.format("Start service %s error. %s",
                    serviceStateManageRequest.getServiceId(), e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    /**
     * Stop the Huawei Cloud Ecs server.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        try {
            EcsClient ecsClient = getEcsClient(serviceStateManageRequest);
            BatchStopServersRequest batchStopServersRequest =
                    converter.buildBatchStopServersRequest(
                            serviceStateManageRequest.getDeployResourceEntityList());
            BatchStopServersResponse response =
                    ecsClient.batchStopServersInvoker(batchStopServersRequest)
                            .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                            .backoffStrategy(huaweiCloudRetryStrategy)
                            .invoke();
            return checkEcsExecResultByJobId(ecsClient, response.getJobId());
        } catch (Exception e) {
            String errorMsg = String.format("Stop service %s error. %s",
                    serviceStateManageRequest.getServiceId(), e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    /**
     * Restart the Huawei Cloud Ecs server.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        try {
            EcsClient ecsClient = getEcsClient(serviceStateManageRequest);
            BatchRebootServersRequest request = converter.buildBatchRebootServersRequest(
                    serviceStateManageRequest.getDeployResourceEntityList());
            BatchRebootServersResponse response = ecsClient.batchRebootServersInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            return checkEcsExecResultByJobId(ecsClient, response.getJobId());
        } catch (Exception e) {
            String errorMsg = String.format("Restart service %s error. %s",
                    serviceStateManageRequest.getServiceId(), e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    private EcsClient getEcsClient(ServiceStateManageRequest serviceStateManageRequest) {
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.HUAWEI, CredentialType.VARIABLES,
                        serviceStateManageRequest.getUserId());
        ICredential icredential = huaweiCloudClient.getCredential(credential);
        return huaweiCloudClient.getEcsClient(icredential,
                serviceStateManageRequest.getRegionName());
    }

    private boolean checkEcsExecResultByJobId(EcsClient ecsClient, String jobId) {
        ShowJobResponse response = ecsClient.showJobInvoker(new ShowJobRequest().withJobId(jobId))
                .retryTimes(WAITING_JOB_SUCCESS_RETRY_TIMES)
                .retryCondition(this::jobIsNotSuccess)
                .backoffStrategy(huaweiCloudRetryStrategy).invoke();
        if (response.getStatus().equals(StatusEnum.FAIL)) {
            String errorMsg = String.format(
                    "Manage vm operation failed. JobId: %s reason: %s " + "message: %s", jobId,
                    response.getFailReason(), response.getMessage());
            throw new ClientApiCallFailedException(errorMsg);
        }
        return response.getStatus().equals(StatusEnum.SUCCESS);
    }

    private boolean jobIsNotSuccess(ShowJobResponse response, Exception ex) {
        if (Objects.nonNull(ex)) {
            return false;
        }
        return response.getStatus() != StatusEnum.SUCCESS;
    }

}