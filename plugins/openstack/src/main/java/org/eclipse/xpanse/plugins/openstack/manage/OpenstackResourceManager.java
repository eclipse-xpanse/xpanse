/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.plugins.openstack.common.keystone.KeystoneManager;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/**
 * Openstack Resource Manager.
 */
@Slf4j
@Component
public class OpenstackResourceManager {

    private final CredentialCenter credentialCenter;
    private final KeystoneManager keystoneManager;

    @Autowired
    public OpenstackResourceManager(
            CredentialCenter credentialCenter,
            KeystoneManager keystoneManager) {
        this.credentialCenter = credentialCenter;
        this.keystoneManager = keystoneManager;
    }

    /**
     * List Openstack resource by the kind of ReusableCloudResource.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<String> getExistingResourceNamesWithKind(String userId,
                                                         String region, DeployResourceKind kind) {
        if (kind == DeployResourceKind.VPC) {
            return getVpcList(userId, region);
        } else if (kind == DeployResourceKind.SUBNET) {
            return getSubnetList(userId, region);
        } else if (kind == DeployResourceKind.SECURITY_GROUP) {
            return getSecurityGroupsList(userId, region);
        } else if (kind == DeployResourceKind.SECURITY_GROUP_RULE) {
            return getSecurityGroupRuleList(userId, region);
        } else if (kind == DeployResourceKind.PUBLIC_IP) {
            return getPublicIpList(userId, region);
        } else if (kind == DeployResourceKind.VOLUME) {
            return getVolumeList(userId, region);
        } else if (kind == DeployResourceKind.KEYPAIR) {
            return getKeyPairsList(userId, region);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * List availability zones of region.
     *
     * @param userId user id
     * @param region region
     * @return availability zones
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<String> getAvailabilityZonesOfRegion(String userId, String region) {
        List<String> availabilityZoneNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().availabilityzone().list().forEach(
                    availabilityZone -> availabilityZoneNames.add(availabilityZone.getName()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listAvailabilityZones with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return availabilityZoneNames;
    }


    private List<String> getVpcList(String userId, String region) {
        List<String> vpcNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().network().list()
                    .forEach(network -> vpcNames.add(network.getName()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listVpcs with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return vpcNames;
    }

    private List<String> getSubnetList(String userId, String region) {
        List<String> subnetNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().subnet().list()
                    .forEach(subnet -> subnetNames.add(subnet.getName()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listSubnets with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return subnetNames;
    }

    private List<String> getSecurityGroupsList(String userId, String region) {
        List<String> securityGroupNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().securitygroup().list()
                    .forEach(securityGroup -> securityGroupNames.add(securityGroup.getName()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listSecurityGroups with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return securityGroupNames;
    }

    private List<String> getSecurityGroupRuleList(String userId, String region) {
        List<String> securityGroupRuleIds = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().securityrule().list().forEach(
                    securityGroupRule -> securityGroupRuleIds.add(securityGroupRule.getId()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listSecurityGroupRules with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return securityGroupRuleIds;
    }

    private List<String> getPublicIpList(String userId, String region) {
        List<String> publicIpAddresses = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().floatingip().list().forEach(
                    floatingIp -> publicIpAddresses.add(floatingIp.getFloatingIpAddress()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listPublicIps with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return publicIpAddresses;
    }

    private List<String> getVolumeList(String userId, String region) {
        List<String> volumeNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.blockStorage().volumes().list()
                    .forEach(volume -> volumeNames.add(volume.getName()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listVolumes with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return volumeNames;
    }

    private List<String> getKeyPairsList(String userId, String region) {
        List<String> keyPairNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.compute().keypairs().list()
                    .forEach(keyPair -> keyPairNames.add(keyPair.getName()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "OpenstackClient listKeyPairs with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return keyPairNames;
    }

    private OSClient.OSClientV3 getOsClient(String userId, String region) {
        AbstractCredentialInfo credentialInfo =
                credentialCenter.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, userId);
        return keystoneManager.getAuthenticatedClient(null, credentialInfo).useRegion(region);
    }
}