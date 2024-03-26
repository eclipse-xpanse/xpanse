/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.manage;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.plugins.openstack.common.keystone.KeystoneManager;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<String> getAvailabilityZonesOfRegion(String userId, String region) {
        List<String> availabilityZones = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().availabilityzone().list()
                    .forEach(availabilityZone -> availabilityZones.add(availabilityZone.getName()));
        } catch (RuntimeException e) {
            log.error("Get Openstack availability zones of region:{} failed, error:{}", region,
                    e.getMessage());
        }
        return availabilityZones;
    }


    private List<String> getVpcList(String userId, String region) {
        List<String> vpcs = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().network().list().forEach(network -> vpcs.add(network.getName()));
        } catch (RuntimeException e) {
            log.error("Get Openstack vpc resources failed, error:{}", e.getMessage());
        }
        return vpcs;
    }

    private List<String> getSubnetList(String userId, String region) {
        List<String> subnets = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().subnet().list().forEach(subnet -> subnets.add(subnet.getName()));
        } catch (RuntimeException e) {
            log.error("Get Openstack subnet resources failed, error:{}", e.getMessage());
        }
        return subnets;
    }

    private List<String> getSecurityGroupsList(String userId, String region) {
        List<String> securityGroups = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().securitygroup().list()
                    .forEach(securityGroup -> securityGroups.add(securityGroup.getName()));
        } catch (RuntimeException e) {
            log.error("Get Openstack SecurityGroup resources failed, error:{}", e.getMessage());
        }
        return securityGroups;
    }

    private List<String> getSecurityGroupRuleList(String userId, String region) {
        List<String> securityGroupRules = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().securityrule().list().forEach(
                    securityGroupRule -> securityGroupRules.add(securityGroupRule.getId()));
        } catch (RuntimeException e) {
            log.error("Get Openstack SecurityGroupRule resources failed, error:{}", e.getMessage());
        }
        return securityGroupRules;
    }

    private List<String> getPublicIpList(String userId, String region) {
        List<String> publicIps = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.networking().floatingip().list()
                    .forEach(floatingIp -> publicIps.add(floatingIp.getFloatingIpAddress()));
        } catch (RuntimeException e) {
            log.error("Get Openstack publicIp resources failed, error:{}", e.getMessage());
        }
        return publicIps;
    }

    private List<String> getVolumeList(String userId, String region) {
        List<String> volumes = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.blockStorage().volumes().list()
                    .forEach(volume -> volumes.add(volume.getName()));
        } catch (RuntimeException e) {
            log.error("Get Openstack volume resources failed, error:{}", e.getMessage());
        }
        return volumes;
    }

    private List<String> getKeyPairsList(String userId, String region) {
        List<String> keyPairs = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(userId, region);
            osClient.compute().keypairs().list()
                    .forEach(keyPair -> keyPairs.add(keyPair.getName()));
        } catch (RuntimeException e) {
            log.error("Get Openstack keyPair resources failed, error:{}", e.getMessage());
        }
        return keyPairs;
    }

    private OSClient.OSClientV3 getOsClient(String userId, String region) {
        AbstractCredentialInfo credentialInfo =
                credentialCenter.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, userId);
        return keystoneManager.getAuthenticatedClient(null, credentialInfo).useRegion(region);
    }
}