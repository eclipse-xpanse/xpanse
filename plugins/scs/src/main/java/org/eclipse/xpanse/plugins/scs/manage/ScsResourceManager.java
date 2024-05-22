/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs.manage;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.plugins.scs.common.keystone.ScsKeystoneManager;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Scs Resource Manager.
 */
@Slf4j
@Component
public class ScsResourceManager {

    private final CredentialCenter credentialCenter;
    private final ScsKeystoneManager scsKeystoneManager;

    @Autowired
    public ScsResourceManager(CredentialCenter credentialCenter,
                              ScsKeystoneManager scsKeystoneManager) {
        this.credentialCenter = credentialCenter;
        this.scsKeystoneManager = scsKeystoneManager;
    }

    /**
     * List Scs resource by the kind of ReusableCloudResource.
     */
    public List<String> getExistingResourceNamesWithKind(String userId, String region,
                                                         DeployResourceKind kind) {
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
    public List<String> getAvailabilityZonesOfRegion(String userId, String region) {
        List<String> availabilityZoneNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().availabilityzone().list().forEach(
                    availabilityZone -> availabilityZoneNames.add(availabilityZone.getName()));
        } catch (Exception e) {
            String errorMsg = String.format(
                    "ScsOpenstackClient listAvailabilityZones with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
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
                    "ScsOpenstackClient listVpcs with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
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
                    "ScsOpenstackClient listSubnets with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
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
                    "ScsOpenstackClient listSecurityGroups with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
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
                    "ScsOpenstackClient listSecurityGroupRules with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
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
                    "ScsOpenstackClient listPublicIps with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
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
                    "ScsOpenstackClient listVolumes with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
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
                    "ScsOpenstackClient listKeyPairs with region %s failed. %s",
                    region, e.getMessage());
            log.error(errorMsg, e);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return keyPairNames;
    }

    private OSClient.OSClientV3 getOsClient(String userId, String region) {
        AbstractCredentialInfo credentialInfo =
                credentialCenter.getCredential(Csp.SCS, CredentialType.VARIABLES, userId);
        return scsKeystoneManager.getAuthenticatedClient(null, credentialInfo).useRegion(region);
    }

}
