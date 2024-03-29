/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.scs.manage;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.scs.common.keystone.ScsKeystoneManager;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to manage state of VMs for SCS plugin.
 */
@Slf4j
@Component
public class ScsServersManager {

    private final ScsKeystoneManager scsKeystoneManager;

    private final CredentialCenter credentialCenter;


    /**
     * Constructor for the MetricsManager bean.
     *
     * @param scsKeystoneManager  KeystoneManager bean.
     * @param credentialCenter credentialCenter bean.
     */
    @Autowired
    public ScsServersManager(ScsKeystoneManager scsKeystoneManager,
                             CredentialCenter credentialCenter) {
        this.scsKeystoneManager = scsKeystoneManager;
        this.credentialCenter = credentialCenter;
    }


    /**
     * Start the SCS VM.
     */
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        OSClient.OSClientV3 osClient = getOsClient(
                serviceStateManageRequest.getUserId(), serviceStateManageRequest.getServiceId());
        int failedCount = 0;
        for (DeployResourceEntity resource
                : serviceStateManageRequest.getDeployResourceEntityList()) {
            Server serverBeforeStart = osClient.compute().servers().get(resource.getResourceId());
            if (serverBeforeStart.getStatus().equals(Server.Status.ACTIVE)) {
                log.info("Server with id {} is already started.", resource.getResourceId());
                continue;
            }
            ActionResponse actionResponse =
                    osClient.compute().servers().action(resource.getResourceId(), Action.START);
            if (actionResponse.isSuccess()) {
                log.info("Start server with id {} successfully.", resource.getResourceId());
            } else {
                failedCount++;
                log.error("Start server with id {} failed. error:{}", resource.getResourceId(),
                        actionResponse.getFault());
            }
        }
        return failedCount == 0;
    }


    /**
     * Stop the SCS VM.
     */
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        OSClient.OSClientV3 osClient = getOsClient(
                serviceStateManageRequest.getUserId(), serviceStateManageRequest.getServiceId());
        int failureCount = 0;
        for (DeployResourceEntity resource
                : serviceStateManageRequest.getDeployResourceEntityList()) {
            Server serverBeforeStop = osClient.compute().servers().get(resource.getResourceId());
            if (serverBeforeStop.getStatus().equals(Server.Status.SHUTOFF)) {
                log.info("Server with id {} is already stopped.", resource.getResourceId());
                continue;
            }
            ActionResponse actionResponse =
                    osClient.compute().servers().action(resource.getResourceId(), Action.STOP);
            if (actionResponse.isSuccess()) {
                log.info("Stop server with id {} successfully.", resource.getResourceId());
            } else {
                failureCount++;
                log.error("Stop server with id {} failed. error:{}", resource.getResourceId(),
                        actionResponse.getFault());
            }
        }
        return failureCount == 0;
    }

    /**
     * Restart the SCS VM.
     */
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        OSClient.OSClientV3 osClient = getOsClient(
                serviceStateManageRequest.getUserId(), serviceStateManageRequest.getServiceId());
        int failureCount = 0;
        for (DeployResourceEntity resource
                : serviceStateManageRequest.getDeployResourceEntityList()) {
            Server serverBeforeRestart = osClient.compute().servers().get(resource.getResourceId());
            if (!serverBeforeRestart.getStatus().equals(Server.Status.ACTIVE)) {
                log.error("Server with id {} could not be restarted when it is inactive.",
                        resource.getResourceId());
                continue;
            }
            ActionResponse actionResponse =
                    osClient.compute().servers().reboot(resource.getResourceId(), RebootType.SOFT);
            if (actionResponse.isSuccess()) {
                log.info("Restart server with id {} successfully.", resource.getResourceId());
            } else {
                failureCount++;
                log.error("Restart server with id {} failed.error:{}", resource.getResourceId(),
                        actionResponse.getFault());
            }
        }
        return failureCount == 0;
    }

    private OSClient.OSClientV3 getOsClient(String userId, UUID serviceId) {
        AbstractCredentialInfo credentialInfo =
                credentialCenter.getCredential(Csp.SCS, CredentialType.VARIABLES, userId);
        return scsKeystoneManager.getAuthenticatedClient(serviceId, credentialInfo);
    }
}



