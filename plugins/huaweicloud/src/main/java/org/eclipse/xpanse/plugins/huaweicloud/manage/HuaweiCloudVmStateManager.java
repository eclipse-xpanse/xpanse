/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.manage;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates all Manager-related public methods of the Huawei Cloud plugin.
 */
@Slf4j
@Component
public class HuaweiCloudVmStateManager {

    private final CredentialCenter credentialCenter;
    private final HuaweiCloudManagerClient huaweiCloudManagerClient;
    private final HuaweiCloudServerManageRequestConverter huaweiCloudServerManageRequestConverter;
    private static final int BASE_DELAY = 1000;
    private static final int MAX_BACK_OFF_IN_MILLISECONDS = 30000;
    private static final int RETRY_TIMES = 10;

    /**
     * Constructs a HuaweiCloudVmStateManager with the necessary dependencies.
     */
    @Autowired
    public HuaweiCloudVmStateManager(
            CredentialCenter credentialCenter,
            HuaweiCloudManagerClient huaweiCloudManagerClient,
            HuaweiCloudServerManageRequestConverter huaweiCloudServerManageRequestConverter) {
        this.credentialCenter = credentialCenter;
        this.huaweiCloudManagerClient = huaweiCloudManagerClient;
        this.huaweiCloudServerManageRequestConverter = huaweiCloudServerManageRequestConverter;
    }

    /**
     * Start the Huawei Cloud Ecs server.
     */
    public boolean startService(ServiceManagerRequest serviceManagerRequest) {
        EcsClient ecsClient = getEcsClient(serviceManagerRequest);
        BatchStartServersRequest request =
                huaweiCloudServerManageRequestConverter.buildBatchStartServersRequest(
                        serviceManagerRequest.getDeployResourceEntityList());
        BatchStartServersResponse response = ecsClient.batchStartServers(request);
        return checkEcsExecResultByJobId(ecsClient, response.getJobId());
    }

    /**
     * Stop the Huawei Cloud Ecs server.
     */
    public boolean stopService(ServiceManagerRequest serviceManagerRequest) {
        EcsClient ecsClient = getEcsClient(serviceManagerRequest);
        BatchStopServersRequest batchStopServersRequest =
                huaweiCloudServerManageRequestConverter.buildBatchStopServersRequest(
                        serviceManagerRequest.getDeployResourceEntityList());
        BatchStopServersResponse response = ecsClient.batchStopServers(batchStopServersRequest);
        return checkEcsExecResultByJobId(ecsClient, response.getJobId());
    }

    /**
     * Restart the Huawei Cloud Ecs server.
     */
    public boolean restartService(ServiceManagerRequest serviceManagerRequest) {
        EcsClient ecsClient = getEcsClient(serviceManagerRequest);
        BatchRebootServersRequest request =
                huaweiCloudServerManageRequestConverter.buildBatchRebootServersRequest(
                        serviceManagerRequest.getDeployResourceEntityList());
        BatchRebootServersResponse response = ecsClient.batchRebootServers(request);
        return checkEcsExecResultByJobId(ecsClient, response.getJobId());
    }

    private EcsClient getEcsClient(ServiceManagerRequest serviceManagerRequest) {
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.HUAWEI, CredentialType.VARIABLES,
                        serviceManagerRequest.getUserId());
        ICredential icredential = huaweiCloudManagerClient.getCredential(credential);
        return huaweiCloudManagerClient.getEcsClient(icredential,
                serviceManagerRequest.getRegionName());
    }

    private boolean checkEcsExecResultByJobId(EcsClient ecsClient, String jobId) {
        ShowJobResponse response = ecsClient.showJobInvoker(new ShowJobRequest().withJobId(jobId))
                .retryTimes(RETRY_TIMES)
                .retryCondition((resp, ex) -> Objects.nonNull(resp) && !resp.getStatus()
                        .equals(ShowJobResponse.StatusEnum.SUCCESS))
                .backoffStrategy(new SdkBackoffStrategy(BASE_DELAY, MAX_BACK_OFF_IN_MILLISECONDS))
                .invoke();
        if (response.getStatus().equals(StatusEnum.FAIL)) {
            log.error("manage vm operation failed. JobId: {} reason: {} message: {}", jobId,
                    response.getFailReason(), response.getMessage());
        }
        return response.getStatus().equals(StatusEnum.SUCCESS);
    }

}