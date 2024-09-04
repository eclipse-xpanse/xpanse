/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.manage;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.openstack.common.auth.ProviderAuthInfoResolver;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class to manage state of VMs for Openstack plugin.
 */
@Slf4j
@Component
public class OpenstackServersManager {

    @Resource
    private ProviderAuthInfoResolver providerAuthInfoResolver;


    /**
     * Start the OpenStack Nova VM.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public boolean startService(Csp csp, ServiceStateManageRequest request) {
        try {
            String userId = request.getUserId();
            UUID serviceId = request.getServiceId();
            String site = request.getRegion().getSite();
            List<String> errorMessages = new ArrayList<>();
            OSClient.OSClientV3 osClient = getOsClient(csp, site, userId, serviceId);
            for (DeployResourceEntity resource : request.getDeployResourceEntityList()) {
                Server serverBeforeStart =
                        osClient.compute().servers().get(resource.getResourceId());
                if (serverBeforeStart.getStatus().equals(Server.Status.ACTIVE)) {
                    log.info("Resource with id {} is already started.", resource.getResourceId());
                    continue;
                }
                ActionResponse actionResponse =
                        osClient.compute().servers().action(resource.getResourceId(), Action.START);
                if (actionResponse.isSuccess()) {
                    log.info("Start resource with id {} successfully.", resource.getResourceId());
                } else {
                    String errorMsg = String.format("Start resource %s failed, error: %s",
                            resource.getResourceId(), actionResponse.getFault());
                    log.error(errorMsg);
                    errorMessages.add(errorMsg);
                }
            }
            if (!CollectionUtils.isEmpty(errorMessages)) {
                throw new ClientApiCallFailedException(String.join("\n", errorMessages));
            }
        } catch (Exception e) {
            log.error("Start service {} failed.", request.getServiceId());
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return true;
    }


    /**
     * Stop the OpenStack Nova VM.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public boolean stopService(Csp csp, ServiceStateManageRequest request) {
        try {
            String site = request.getRegion().getSite();
            String userId = request.getUserId();
            UUID serviceId = request.getServiceId();
            OSClient.OSClientV3 osClient = getOsClient(csp, site, userId, serviceId);
            List<String> errorMessages = new ArrayList<>();
            for (DeployResourceEntity resource : request.getDeployResourceEntityList()) {
                Server serverBeforeStop =
                        osClient.compute().servers().get(resource.getResourceId());
                if (serverBeforeStop.getStatus().equals(Server.Status.SHUTOFF)) {
                    log.info("Resource with id {} is already stopped.", resource.getResourceId());
                    continue;
                }
                ActionResponse actionResponse =
                        osClient.compute().servers().action(resource.getResourceId(), Action.STOP);
                if (actionResponse.isSuccess()) {
                    log.info("Stop resource {} successfully.", resource.getResourceId());
                } else {
                    String errorMsg = String.format("Stop resource %s failed, error: %s",
                            resource.getResourceId(), actionResponse.getFault());
                    log.error(errorMsg);
                    errorMessages.add(errorMsg);
                }
            }
            if (!CollectionUtils.isEmpty(errorMessages)) {
                throw new ClientApiCallFailedException(String.join("\n", errorMessages));
            }
            return true;
        } catch (Exception e) {
            log.error("Stop service {} failed.", request.getServiceId());
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    /**
     * Restart the OpenStack Nova VM.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public boolean restartService(Csp csp, ServiceStateManageRequest request) {
        try {
            String site = request.getRegion().getSite();
            String userId = request.getUserId();
            UUID serviceId = request.getServiceId();
            OSClient.OSClientV3 osClient = getOsClient(csp, site, userId, serviceId);
            List<String> errorMessages = new ArrayList<>();
            for (DeployResourceEntity resource : request.getDeployResourceEntityList()) {
                Server serverBeforeRestart =
                        osClient.compute().servers().get(resource.getResourceId());
                if (!serverBeforeRestart.getStatus().equals(Server.Status.ACTIVE)) {
                    log.error("Resource with id {} could not be restarted when it is inactive.",
                            resource.getResourceId());
                    continue;
                }
                ActionResponse actionResponse = osClient.compute().servers()
                        .reboot(resource.getResourceId(), RebootType.SOFT);
                if (actionResponse.isSuccess()) {
                    log.info("Restart resource with id {} successfully.", resource.getResourceId());
                } else {
                    String errorMsg = String.format("Restart resource %s failed, error: %s",
                            resource.getResourceId(), actionResponse.getFault());
                    log.error(errorMsg);
                    errorMessages.add(errorMsg);
                }
            }
            if (!CollectionUtils.isEmpty(errorMessages)) {
                throw new ClientApiCallFailedException(String.join("\n", errorMessages));
            }
            return true;
        } catch (Exception e) {
            log.error("Restart service {} failed.", request.getServiceId());
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    private OSClient.OSClientV3 getOsClient(Csp csp, String site, String userId, UUID serviceId) {
        return providerAuthInfoResolver.getAuthenticatedClientForCsp(csp, site, userId, serviceId);
    }
}



