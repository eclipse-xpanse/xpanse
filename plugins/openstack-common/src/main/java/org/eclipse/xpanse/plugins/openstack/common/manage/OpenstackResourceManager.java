/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.manage;

import com.bertramlabs.plugins.hcl4j.HCLParser;
import com.bertramlabs.plugins.hcl4j.HCLParserException;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.plugins.openstack.common.auth.ProviderAuthInfoResolver;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Openstack Resource Manager.
 */
@Slf4j
@Component
public class OpenstackResourceManager {
    public static final String RESOURCE = "resource";
    public static final String OPENSTACK_COMPUTE_INSTANCE = "openstack_compute_instance_v2";
    @Resource
    private ProviderAuthInfoResolver providerAuthInfoResolver;


    /**
     * List Openstack resource by the kind of ReusableCloudResource.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<String> getExistingResourceNamesWithKind(Csp csp, String userId, UUID serviceId,
                                                         String region, DeployResourceKind kind) {

        if (kind == DeployResourceKind.VPC) {
            return getVpcList(csp, userId, serviceId, region);
        } else if (kind == DeployResourceKind.SUBNET) {
            return getSubnetList(csp, userId, serviceId, region);
        } else if (kind == DeployResourceKind.SECURITY_GROUP) {
            return getSecurityGroupsList(csp, userId, serviceId, region);
        } else if (kind == DeployResourceKind.SECURITY_GROUP_RULE) {
            return getSecurityGroupRuleList(csp, userId, serviceId, region);
        } else if (kind == DeployResourceKind.PUBLIC_IP) {
            return getPublicIpList(csp, userId, serviceId, region);
        } else if (kind == DeployResourceKind.VOLUME) {
            return getVolumeList(csp, userId, serviceId, region);
        } else if (kind == DeployResourceKind.KEYPAIR) {
            return getKeyPairsList(csp, userId, serviceId, region);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * List availability zones of region.
     *
     * @return availability zones
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<String> getAvailabilityZonesOfRegion(Csp csp, String userId, UUID serviceId,
                                                     String region) {
        List<String> availabilityZoneNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.networking().availabilityzone().list().forEach(
                    availabilityZone -> availabilityZoneNames.add(availabilityZone.getName()));
        } catch (Exception e) {
            log.error("OpenstackClient listAvailabilityZones with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return availabilityZoneNames;
    }


    private List<String> getVpcList(Csp csp, String userId, UUID serviceId, String region) {
        List<String> vpcNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.networking().network().list()
                    .forEach(network -> vpcNames.add(network.getName()));
        } catch (Exception e) {
            log.error("OpenstackClient listVpcs with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return vpcNames;
    }

    private List<String> getSubnetList(Csp csp, String userId, UUID serviceId, String region) {
        List<String> subnetNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.networking().subnet().list()
                    .forEach(subnet -> subnetNames.add(subnet.getName()));
        } catch (Exception e) {
            log.error("OpenstackClient listSubnets with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return subnetNames;
    }

    private List<String> getSecurityGroupsList(Csp csp, String userId, UUID serviceId,
                                               String region) {
        List<String> securityGroupNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.networking().securitygroup().list()
                    .forEach(securityGroup -> securityGroupNames.add(securityGroup.getName()));
        } catch (Exception e) {
            log.error("OpenstackClient listSecurityGroups with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return securityGroupNames;
    }

    private List<String> getSecurityGroupRuleList(Csp csp, String userId, UUID serviceId,
                                                  String region) {
        List<String> securityGroupRuleIds = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.networking().securityrule().list().forEach(
                    securityGroupRule -> securityGroupRuleIds.add(securityGroupRule.getId()));
        } catch (Exception e) {
            log.error("OpenstackClient listSecurityGroupRules with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return securityGroupRuleIds;
    }

    private List<String> getPublicIpList(Csp csp, String userId, UUID serviceId,
                                         String region) {
        List<String> publicIpAddresses = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.networking().floatingip().list().forEach(
                    floatingIp -> publicIpAddresses.add(floatingIp.getFloatingIpAddress()));
        } catch (Exception e) {
            log.error("OpenstackClient listPublicIps with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return publicIpAddresses;
    }

    private List<String> getVolumeList(Csp csp, String userId, UUID serviceId,
                                       String region) {
        List<String> volumeNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.blockStorage().volumes().list()
                    .forEach(volume -> volumeNames.add(volume.getName()));
        } catch (Exception e) {
            log.error("OpenstackClient listVolumes with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return volumeNames;
    }

    private List<String> getKeyPairsList(Csp csp, String userId, UUID serviceId, String region) {
        List<String> keyPairNames = new ArrayList<>();
        try {
            OSClientV3 osClient = getOsClient(csp, userId, serviceId, region);
            osClient.compute().keypairs().list()
                    .forEach(keyPair -> keyPairNames.add(keyPair.getName()));
        } catch (Exception e) {
            log.error("OpenstackClient listKeyPairs with region {} failed.", region);
            providerAuthInfoResolver.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return keyPairNames;
    }

    private OSClient.OSClientV3 getOsClient(Csp csp, String userId, UUID serviceId, String region) {
        return providerAuthInfoResolver.getAuthenticatedClientForCsp(csp, userId, serviceId)
                .useRegion(region);
    }

    /**
     * get resources name for service deployment.
     */
    public Map<String, String> getComputeResourcesInServiceDeployment(File scriptFile) {
        Map<String, Object> results;
        Map<String, String> resources = new HashMap<>();
        try {
            results = new HCLParser().parse(scriptFile, "UTF-8");
            if (!CollectionUtils.isEmpty(results) && results.containsKey(RESOURCE)) {
                Map<String, Object> resourceMap = (Map<String, Object>) results.get(RESOURCE);
                if (!CollectionUtils.isEmpty(resourceMap)
                        && resourceMap.containsKey(OPENSTACK_COMPUTE_INSTANCE)) {
                    Map<String, Object> resourceInfoMap =
                            (Map<String, Object>) resourceMap.get(OPENSTACK_COMPUTE_INSTANCE);
                    if (!CollectionUtils.isEmpty(resourceInfoMap)) {
                        Set<String> resourceNameSet = resourceInfoMap.keySet();
                        resourceNameSet.forEach(resourceName -> {
                            resources.put(resourceName, OPENSTACK_COMPUTE_INSTANCE);
                        });
                    }
                }
            }
        } catch (HCLParserException | IOException e) {
            String error = String.format("Hcl4j parse terraform.tf file error, error %s .",
                    e.getMessage());
            log.error(error);
            throw new TerraformScriptFormatInvalidException(List.of(error));
        }
        return resources;
    }
}