/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.manage;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.manage.ServiceManagerRequest;
import org.eclipse.xpanse.plugins.openstack.common.keystone.KeystoneManager;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to manage state of VMs for Openstack plugin.
 */
@Slf4j
@Component
public class ServersManager {

    private final KeystoneManager keystoneManager;

    private final CredentialCenter credentialCenter;


    /**
     * Constructor for the MetricsManager bean.
     *
     * @param keystoneManager  KeystoneManager bean.
     * @param credentialCenter credentialCenter bean.
     */
    @Autowired
    public ServersManager(KeystoneManager keystoneManager, CredentialCenter credentialCenter) {
        this.keystoneManager = keystoneManager;
        this.credentialCenter = credentialCenter;
    }


    /**
     * Start the OpenStack Ecs server.
     */
    public boolean startService(ServiceManagerRequest serviceManagerRequest) {
        OSClient.OSClientV3 osClient = getOsClient(
                serviceManagerRequest.getUserId(), serviceManagerRequest.getServiceId());
        int failedCount = 0;
        for (DeployResourceEntity resource : serviceManagerRequest.getDeployResourceEntityList()) {
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
     * Stop the OpenStack Ecs server.
     */
    public boolean stopService(ServiceManagerRequest serviceManagerRequest) {
        OSClient.OSClientV3 osClient = getOsClient(
                serviceManagerRequest.getUserId(), serviceManagerRequest.getServiceId());
        int failureCount = 0;
        for (DeployResourceEntity resource : serviceManagerRequest.getDeployResourceEntityList()) {
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
     * Restart the OpenStack Ecs server.
     */
    public boolean restartService(ServiceManagerRequest serviceManagerRequest) {
        OSClient.OSClientV3 osClient = getOsClient(
                serviceManagerRequest.getUserId(), serviceManagerRequest.getServiceId());
        int failureCount = 0;
        for (DeployResourceEntity resource : serviceManagerRequest.getDeployResourceEntityList()) {
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
                credentialCenter.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, userId);
        return keystoneManager.getAuthenticatedClient(serviceId, credentialInfo);
    }
}



