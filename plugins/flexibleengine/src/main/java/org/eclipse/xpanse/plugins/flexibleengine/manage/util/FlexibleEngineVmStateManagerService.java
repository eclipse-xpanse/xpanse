/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.manage.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaweicloud.sdk.core.internal.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.core.internal.model.Project;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse.StatusEnum;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.manage.ServiceManagerRequest;
import org.eclipse.xpanse.plugins.flexibleengine.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.RetryTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class that encapsulates all Manager-related public methods of the FlexibleEngine plugin.
 */
@Slf4j
@Component
public class FlexibleEngineVmStateManagerService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RetryTemplateService retryTemplateService;

    private final CredentialCenter credentialCenter;

    private final FlexibleEngineClient flexibleEngineClient;

    private final FlexibleEngineManageConverter flexibleEngineManageConverter;

    /**
     * Constructs a FlexibleEngineVmStateManager with the necessary dependencies.
     */
    @Autowired
    public FlexibleEngineVmStateManagerService(
            RetryTemplateService retryTemplateService,
            CredentialCenter credentialCenter,
            FlexibleEngineClient flexibleEngineClient,
            FlexibleEngineManageConverter flexibleEngineManageConverter) {
        this.retryTemplateService = retryTemplateService;
        this.credentialCenter = credentialCenter;
        this.flexibleEngineClient = flexibleEngineClient;
        this.flexibleEngineManageConverter = flexibleEngineManageConverter;
    }

    /**
     * Start the Flexible Engine Ecs server.
     */
    public boolean startService(ServiceManagerRequest serviceManagerRequest) {
        try {
            AbstractCredentialInfo credential = credentialCenter.getCredential(Csp.FLEXIBLE_ENGINE,
                    CredentialType.VARIABLES, serviceManagerRequest.getUserId());
            String regionName = serviceManagerRequest.getRegionName();
            Project project = getProjectInfoByRegion(credential, regionName);
            String url =
                    flexibleEngineManageConverter.getManageServiceUrl(regionName, project.getId());
            BatchStartServersRequest request =
                    flexibleEngineManageConverter.buildBatchStartServersRequest(
                            serviceManagerRequest.getDeployResourceEntityList());
            String requestBody = OBJECT_MAPPER.writeValueAsString(request.getBody());
            HttpRequestBase requestBase =
                    flexibleEngineClient.buildPostRequest(credential, url, requestBody);
            ResponseEntity<BatchStartServersResponse> response =
                    retryTemplateService.startService(requestBase, requestBody);
            return checkEcsExecResultByJobId(credential, regionName, project.getId(),
                    response.getBody().getJobId());
        } catch (Exception e) {
            String errorMsg =
                    String.format("Start service by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    /**
     * Stop the Flexible Engine Ecs server.
     */
    public boolean stopService(ServiceManagerRequest serviceManagerRequest) {
        try {
            AbstractCredentialInfo credential = credentialCenter.getCredential(Csp.FLEXIBLE_ENGINE,
                    CredentialType.VARIABLES, serviceManagerRequest.getUserId());
            String regionName = serviceManagerRequest.getRegionName();
            Project project = getProjectInfoByRegion(credential, regionName);
            String url =
                    flexibleEngineManageConverter.getManageServiceUrl(regionName, project.getId());
            BatchStopServersRequest request =
                    flexibleEngineManageConverter.buildBatchStopServersRequest(
                            serviceManagerRequest.getDeployResourceEntityList());
            String requestBody = OBJECT_MAPPER.writeValueAsString(request.getBody());

            HttpRequestBase requestBase =
                    flexibleEngineClient.buildPostRequest(credential, url, requestBody);
            ResponseEntity<BatchStopServersResponse> response =
                    retryTemplateService.stopService(requestBase, requestBody);
            return checkEcsExecResultByJobId(credential, regionName, project.getId(),
                    response.getBody().getJobId());
        } catch (Exception e) {
            String errorMsg =
                    String.format("Stop service by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    /**
     * Restart the Flexible Engine Ecs server.
     */
    public boolean restartService(ServiceManagerRequest serviceManagerRequest) {
        try {
            AbstractCredentialInfo credential = credentialCenter.getCredential(Csp.FLEXIBLE_ENGINE,
                    CredentialType.VARIABLES, serviceManagerRequest.getUserId());
            String regionName = serviceManagerRequest.getRegionName();
            Project project = getProjectInfoByRegion(credential, regionName);
            String url =
                    flexibleEngineManageConverter.getManageServiceUrl(regionName, project.getId());
            BatchRebootServersRequest request =
                    flexibleEngineManageConverter.buildBatchRebootServersRequest(
                            serviceManagerRequest.getDeployResourceEntityList());
            String requestBody = OBJECT_MAPPER.writeValueAsString(request.getBody());
            HttpRequestBase requestBase =
                    flexibleEngineClient.buildPostRequest(credential, url, requestBody);
            ResponseEntity<BatchRebootServersResponse> response =
                    retryTemplateService.restartService(requestBase, requestBody);

            return checkEcsExecResultByJobId(credential, regionName, project.getId(),
                    response.getBody().getJobId());
        } catch (Exception e) {
            String errorMsg =
                    String.format("Restart service by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    private boolean checkEcsExecResultByJobId(AbstractCredentialInfo credential,
            String regionName, String projectId, String jobId) {
        try {
            String url = flexibleEngineManageConverter.getCheckJobStatusUrl(regionName, projectId,
                    jobId);
            HttpRequestBase requestBase =
                    flexibleEngineClient.buildGetRequest(credential, url);
            ResponseEntity<ShowJobResponse> response =
                    retryTemplateService.checkEcsExecResultByJobId(requestBase);
            if (response.getBody().getStatus().equals(StatusEnum.FAIL)) {
                log.error("manage vm operation failed. JobId: {} reason: {} message: {}", jobId,
                        response.getBody().getFailReason(), response.getBody().getMessage());
            }
            return response.getStatusCode().equals(HttpStatus.OK);
        } catch (Exception e) {
            String errorMsg =
                    String.format("manage vm operation by FlexibleEngine Client error. %s",
                            e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }

    private Project getProjectInfoByRegion(AbstractCredentialInfo credential, String region) {
        Project project = null;
        if (StringUtils.isNotBlank(region)) {
            String projectQueryUrl =
                    flexibleEngineManageConverter.buildProjectQueryUrl(region).toString();
            project =
                    queryProjectInfo(credential, projectQueryUrl);
        }
        if (Objects.isNull(project) || StringUtils.isBlank(project.getId())) {
            throw new ClientApiCallFailedException(
                    "Query project info by FlexibleEngine Client failed. Project info is null.");
        }
        return project;
    }

    private Project queryProjectInfo(AbstractCredentialInfo credential, String url) {
        try {
            HttpRequestBase requestBase =
                    flexibleEngineClient.buildGetRequest(credential, url);
            KeystoneListProjectsResponse projectsResponse =
                    retryTemplateService.queryProjectInfo(requestBase);
            if (Objects.nonNull(projectsResponse)
                    && !CollectionUtils.isEmpty(projectsResponse.getProjects())) {
                return projectsResponse.getProjects().get(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            String errorMsg = String.format("Query project info by FlexibleEngine Client error. %s",
                    e.getMessage());
            log.error(errorMsg);
            throw new ClientApiCallFailedException(errorMsg);
        }
    }
}
