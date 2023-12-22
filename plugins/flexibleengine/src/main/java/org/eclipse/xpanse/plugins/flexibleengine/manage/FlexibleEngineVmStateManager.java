/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.manage;

import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.DEFAULT_DELAY_MILLIS;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.DEFAULT_RETRY_TIMES;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.retry.backoff.SdkBackoffStrategy;
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
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.manage.ServiceManagerRequest;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates all Manager-related public methods of the Huawei Cloud plugin.
 */
@Slf4j
@Component
public class FlexibleEngineVmStateManager {

    private static final int BASE_DELAY = 1000;
    private static final int MAX_BACK_OFF_IN_MILLISECONDS = 30000;
    private static final int RETRY_TIMES = 10;
    private final CredentialCenter credentialCenter;
    private final FlexibleEngineClient flexibleEngineClient;
    private final FlexibleEngineServerManageRequestConverter converter;

    /**
     * Constructs a HuaweiCloudVmStateManager with the necessary dependencies.
     */
    @Autowired
    public FlexibleEngineVmStateManager(CredentialCenter credentialCenter,
                                        FlexibleEngineClient flexibleEngineClient,
                                        FlexibleEngineServerManageRequestConverter converter) {
        this.credentialCenter = credentialCenter;
        this.flexibleEngineClient = flexibleEngineClient;
        this.converter = converter;
    }

    /**
     * Start the Huawei Cloud Ecs server.
     */
    public boolean startService(ServiceManagerRequest serviceManagerRequest) {
        EcsClient ecsClient = getEcsClient(serviceManagerRequest);
        BatchStartServersRequest request =
                converter.buildBatchStartServersRequest(
                        serviceManagerRequest.getDeployResourceEntityList());
        BatchStartServersResponse response =
                ecsClient.batchStartServersInvoker(request).retryTimes(DEFAULT_RETRY_TIMES)
                        .retryCondition(flexibleEngineClient::matchRetryCondition)
                        .backoffStrategy(new FlexibleEngineRetryStrategy(DEFAULT_DELAY_MILLIS))
                        .invoke();
        return checkEcsExecResultByJobId(ecsClient, response.getJobId());
    }

    /**
     * Stop the Huawei Cloud Ecs server.
     */
    public boolean stopService(ServiceManagerRequest serviceManagerRequest) {
        EcsClient ecsClient = getEcsClient(serviceManagerRequest);
        BatchStopServersRequest batchStopServersRequest =
                converter.buildBatchStopServersRequest(
                        serviceManagerRequest.getDeployResourceEntityList());
        BatchStopServersResponse response =
                ecsClient.batchStopServersInvoker(batchStopServersRequest)
                        .retryTimes(DEFAULT_RETRY_TIMES)
                        .retryCondition(flexibleEngineClient::matchRetryCondition)
                        .backoffStrategy(new FlexibleEngineRetryStrategy(DEFAULT_DELAY_MILLIS))
                        .invoke();
        return checkEcsExecResultByJobId(ecsClient, response.getJobId());
    }

    /**
     * Restart the Huawei Cloud Ecs server.
     */
    public boolean restartService(ServiceManagerRequest serviceManagerRequest) {
        EcsClient ecsClient = getEcsClient(serviceManagerRequest);
        BatchRebootServersRequest request =
                converter.buildBatchRebootServersRequest(
                        serviceManagerRequest.getDeployResourceEntityList());
        BatchRebootServersResponse response =
                ecsClient.batchRebootServersInvoker(request).retryTimes(DEFAULT_RETRY_TIMES)
                        .retryCondition(flexibleEngineClient::matchRetryCondition)
                        .backoffStrategy(new FlexibleEngineRetryStrategy(DEFAULT_DELAY_MILLIS))
                        .invoke();
        return checkEcsExecResultByJobId(ecsClient, response.getJobId());
    }

    private EcsClient getEcsClient(ServiceManagerRequest serviceManagerRequest) {
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES,
                        serviceManagerRequest.getUserId());
        ICredential icredential = flexibleEngineClient.getCredential(credential);
        return flexibleEngineClient.getEcsClient(icredential,
                serviceManagerRequest.getRegionName());
    }

    private boolean checkEcsExecResultByJobId(EcsClient ecsClient, String jobId) {
        ShowJobResponse response = ecsClient.showJobInvoker(new ShowJobRequest().withJobId(jobId))
                .retryTimes(RETRY_TIMES).retryCondition(
                        (resp, ex) -> Objects.nonNull(resp) && !resp.getStatus()
                                .equals(StatusEnum.SUCCESS))
                .backoffStrategy(new SdkBackoffStrategy(BASE_DELAY, MAX_BACK_OFF_IN_MILLISECONDS))
                .invoke();
        if (response.getStatus().equals(StatusEnum.FAIL)) {
            log.error("manage vm operation failed. JobId: {} reason: {} message: {}", jobId,
                    response.getFailReason(), response.getMessage());
        }
        return response.getStatus().equals(StatusEnum.SUCCESS);
    }

}