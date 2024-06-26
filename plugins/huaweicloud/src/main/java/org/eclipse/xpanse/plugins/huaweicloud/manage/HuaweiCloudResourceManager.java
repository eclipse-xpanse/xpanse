/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.huaweicloud.manage;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.model.NovaAvailabilityZone;
import com.huaweicloud.sdk.ecs.v2.model.NovaListAvailabilityZonesRequest;
import com.huaweicloud.sdk.ecs.v2.model.NovaListAvailabilityZonesResponse;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsRequest;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsResponse;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsResult;
import com.huaweicloud.sdk.ecs.v2.model.NovaSimpleKeypair;
import com.huaweicloud.sdk.eip.v2.EipClient;
import com.huaweicloud.sdk.eip.v2.model.ListPublicipsRequest;
import com.huaweicloud.sdk.eip.v2.model.ListPublicipsResponse;
import com.huaweicloud.sdk.eip.v2.model.PublicipShowResp;
import com.huaweicloud.sdk.evs.v2.EvsClient;
import com.huaweicloud.sdk.evs.v2.model.ListVolumesRequest;
import com.huaweicloud.sdk.evs.v2.model.ListVolumesResponse;
import com.huaweicloud.sdk.evs.v2.model.VolumeDetail;
import com.huaweicloud.sdk.vpc.v2.VpcClient;
import com.huaweicloud.sdk.vpc.v2.model.ListSecurityGroupRulesRequest;
import com.huaweicloud.sdk.vpc.v2.model.ListSecurityGroupRulesResponse;
import com.huaweicloud.sdk.vpc.v2.model.ListSecurityGroupsRequest;
import com.huaweicloud.sdk.vpc.v2.model.ListSecurityGroupsResponse;
import com.huaweicloud.sdk.vpc.v2.model.ListSubnetsRequest;
import com.huaweicloud.sdk.vpc.v2.model.ListSubnetsResponse;
import com.huaweicloud.sdk.vpc.v2.model.ListVpcsRequest;
import com.huaweicloud.sdk.vpc.v2.model.ListVpcsResponse;
import com.huaweicloud.sdk.vpc.v2.model.SecurityGroup;
import com.huaweicloud.sdk.vpc.v2.model.SecurityGroupRule;
import com.huaweicloud.sdk.vpc.v2.model.Subnet;
import com.huaweicloud.sdk.vpc.v2.model.Vpc;
import jakarta.annotation.Resource;
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
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/**
 * Huawei Cloud Resource Manager.
 */
@Slf4j
@Component
public class HuaweiCloudResourceManager {

    @Resource
    private CredentialCenter credentialCenter;
    @Resource
    private HuaweiCloudClient huaweiCloudClient;
    @Resource
    private HuaweiCloudRetryStrategy huaweiCloudRetryStrategy;

    /**
     * List HuaweiCloud resource by the kind of ReusableCloudResource.
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
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
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<String> getAvailabilityZonesOfRegion(String userId, String region) {
        List<String> availabilityZoneNames = new ArrayList<>();
        try {
            EcsClient ecsClient = getEcsClient(userId, region);
            NovaListAvailabilityZonesRequest request = new NovaListAvailabilityZonesRequest();
            NovaListAvailabilityZonesResponse response =
                    ecsClient.novaListAvailabilityZonesInvoker(request)
                            .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                            .backoffStrategy(huaweiCloudRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                availabilityZoneNames = response.getAvailabilityZoneInfo()
                        .stream().map(NovaAvailabilityZone::getZoneName).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listAvailabilityZones with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return availabilityZoneNames;
    }

    private List<String> getVpcList(String userId, String region) {
        List<String> vpcNames = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListVpcsRequest request = new ListVpcsRequest();
            ListVpcsResponse response = vpcClient.listVpcsInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            if (response.getHttpStatusCode() == 200) {
                vpcNames = response.getVpcs().stream().map(Vpc::getName).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listVpcs with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return vpcNames;
    }

    private List<String> getSubnetList(String userId, String region) {
        List<String> subnetNames = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListSubnetsRequest request = new ListSubnetsRequest();
            ListSubnetsResponse response = vpcClient.listSubnetsInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            if (response.getHttpStatusCode() == 200) {
                subnetNames = response.getSubnets().stream().map(Subnet::getName).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listSubnets with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return subnetNames;
    }

    private List<String> getSecurityGroupsList(String userId, String region) {
        List<String> securityGroupNames = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListSecurityGroupsRequest request = new ListSecurityGroupsRequest();
            ListSecurityGroupsResponse response = vpcClient.listSecurityGroupsInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            if (response.getHttpStatusCode() == 200) {
                securityGroupNames = response.getSecurityGroups()
                        .stream().map(SecurityGroup::getName).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listSecurityGroups with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return securityGroupNames;
    }

    private List<String> getSecurityGroupRuleList(String userId, String region) {
        List<String> securityGroupRuleIds = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListSecurityGroupRulesRequest request = new ListSecurityGroupRulesRequest();
            ListSecurityGroupRulesResponse response =
                    vpcClient.listSecurityGroupRulesInvoker(request)
                            .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                            .backoffStrategy(huaweiCloudRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                securityGroupRuleIds = response.getSecurityGroupRules()
                        .stream().map(SecurityGroupRule::getId).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listSecurityGroupRules with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return securityGroupRuleIds;
    }

    private List<String> getPublicIpList(String userId, String region) {
        List<String> publicIpAddresses = new ArrayList<>();
        try {
            EipClient eipClient = getEipClient(userId, region);
            ListPublicipsRequest request = new ListPublicipsRequest();
            ListPublicipsResponse response = eipClient.listPublicipsInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            if (response.getHttpStatusCode() == 200) {
                publicIpAddresses = response.getPublicips()
                        .stream().map(PublicipShowResp::getPublicIpAddress).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listPublicIps with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return publicIpAddresses;
    }

    private List<String> getVolumeList(String userId, String region) {
        List<String> volumeNames = new ArrayList<>();
        try {
            EvsClient evsClient = getEvsClient(userId, region);
            ListVolumesRequest request = new ListVolumesRequest();
            ListVolumesResponse response = evsClient.listVolumesInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            if (response.getHttpStatusCode() == 200) {
                volumeNames = response.getVolumes().stream().map(VolumeDetail::getName).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listVolumes with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return volumeNames;
    }

    private List<String> getKeyPairsList(String userId, String region) {
        List<String> keyPairNames = new ArrayList<>();
        try {
            EcsClient ecsClient = getEcsClient(userId, region);
            NovaListKeypairsRequest request = new NovaListKeypairsRequest();
            NovaListKeypairsResponse response = ecsClient.novaListKeypairsInvoker(request)
                    .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                    .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                    .backoffStrategy(huaweiCloudRetryStrategy)
                    .invoke();
            if (response.getHttpStatusCode() == 200) {
                keyPairNames = response.getKeypairs().stream()
                        .map(NovaListKeypairsResult::getKeypair)
                        .map(NovaSimpleKeypair::getName).toList();
            }
        } catch (Exception e) {
            String errorMsg = String.format(
                    "HuaweiCloudClient listKeyPairs with region %s failed. %s",
                    region, e.getMessage());
            int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                    ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
            log.error(errorMsg + " Retry count:" + retryCount);
            throw new ClientApiCallFailedException(errorMsg);
        }
        return keyPairNames;
    }

    private EcsClient getEcsClient(String userId, String regionName) {
        ICredential credential = getCredential(userId);
        return huaweiCloudClient.getEcsClient(credential, regionName);
    }

    private VpcClient getVpcClient(String userId, String regionName) {
        ICredential credential = getCredential(userId);
        return huaweiCloudClient.getVpcClient(credential, regionName);
    }

    private EipClient getEipClient(String userId, String regionName) {
        ICredential credential = getCredential(userId);
        return huaweiCloudClient.getEipClient(credential, regionName);
    }

    private EvsClient getEvsClient(String userId, String regionName) {
        ICredential credential = getCredential(userId);
        return huaweiCloudClient.getEvsClient(credential, regionName);
    }

    private ICredential getCredential(String userId) {
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.HUAWEI_CLOUD, CredentialType.VARIABLES, userId);
        return huaweiCloudClient.getCredential(credential);
    }
}
