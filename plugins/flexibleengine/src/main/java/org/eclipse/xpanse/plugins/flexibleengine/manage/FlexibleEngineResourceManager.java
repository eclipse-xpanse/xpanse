/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.flexibleengine.manage;

import com.bertramlabs.plugins.hcl4j.HCLParser;
import com.bertramlabs.plugins.hcl4j.HCLParserException;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.model.ListServerAzInfo;
import com.huaweicloud.sdk.ecs.v2.model.ListServerAzInfoRequest;
import com.huaweicloud.sdk.ecs.v2.model.ListServerAzInfoResponse;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** FlexibleEngine Resource Manager. */
@Slf4j
@Component
public class FlexibleEngineResourceManager {

    public static final String RESOURCE = "resource";
    public static final String FLEXIBLE_ENGINE_COMPUTE_INSTANCE =
            "flexibleengine_compute_instance_v2";
    @Resource private CredentialCenter credentialCenter;
    @Resource private FlexibleEngineClient flexibleEngineClient;
    @Resource private FlexibleEngineRetryStrategy flexibleEngineRetryStrategy;

    /** List FlexibleEngine resource by the kind of ReusableCloudResource. */
    @Retryable(
            retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<String> getExistingResourceNamesWithKind(
            String siteName, String regionName, String userId, DeployResourceKind kind) {
        if (kind == DeployResourceKind.VPC) {
            return getVpcList(siteName, regionName, userId);
        } else if (kind == DeployResourceKind.SUBNET) {
            return getSubnetList(siteName, regionName, userId);
        } else if (kind == DeployResourceKind.SECURITY_GROUP) {
            return getSecurityGroupsList(siteName, regionName, userId);
        } else if (kind == DeployResourceKind.SECURITY_GROUP_RULE) {
            return getSecurityGroupRuleList(siteName, regionName, userId);
        } else if (kind == DeployResourceKind.PUBLIC_IP) {
            return getPublicIpList(siteName, regionName, userId);
        } else if (kind == DeployResourceKind.VOLUME) {
            return getVolumeList(siteName, regionName, userId);
        } else if (kind == DeployResourceKind.KEYPAIR) {
            return getKeyPairsList(siteName, regionName, userId);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get available zones of the region.
     *
     * @param siteName siteName
     * @param regionName regionName
     * @param userId userId
     * @return List of available zones.
     */
    @Retryable(
            retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<String> getAvailabilityZonesOfRegion(
            String siteName, String regionName, String userId) {
        List<String> availabilityZoneNames = new ArrayList<>();
        try {
            EcsClient ecsClient = getEcsClient(siteName, regionName, userId);
            ListServerAzInfoRequest request = new ListServerAzInfoRequest();
            ListServerAzInfoResponse response =
                    ecsClient
                            .listServerAzInfoInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                availabilityZoneNames =
                        response.getAvailabilityZones().stream()
                                .map(ListServerAzInfo::getAvailabilityZoneId)
                                .toList();
            }
        } catch (Exception e) {
            log.error(
                    "FlexibleEngineClient listAvailabilityZones with region {} failed.",
                    regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return availabilityZoneNames;
    }

    private List<String> getVpcList(String siteName, String regionName, String userId) {
        List<String> vpcNames = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(siteName, regionName, userId);
            ListVpcsRequest request = new ListVpcsRequest();
            ListVpcsResponse response =
                    vpcClient
                            .listVpcsInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                vpcNames = response.getVpcs().stream().map(Vpc::getName).toList();
            }
        } catch (Exception e) {
            log.error("FlexibleEngineClient listVpcs with region {} failed.", regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return vpcNames;
    }

    private List<String> getSubnetList(String siteName, String regionName, String userId) {
        List<String> subnetNames = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(siteName, regionName, userId);
            ListSubnetsRequest request = new ListSubnetsRequest();
            ListSubnetsResponse response =
                    vpcClient
                            .listSubnetsInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                subnetNames = response.getSubnets().stream().map(Subnet::getName).toList();
            }
        } catch (Exception e) {
            log.error("FlexibleEngineClient listSubnets with region {} failed.", regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return subnetNames;
    }

    private List<String> getSecurityGroupsList(String siteName, String regionName, String userId) {
        List<String> securityGroupNames = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(siteName, regionName, userId);
            ListSecurityGroupsRequest request = new ListSecurityGroupsRequest();
            ListSecurityGroupsResponse response =
                    vpcClient
                            .listSecurityGroupsInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                securityGroupNames =
                        response.getSecurityGroups().stream().map(SecurityGroup::getName).toList();
            }
        } catch (Exception e) {
            log.error("FlexibleEngineClient listSecurityGroups with region {} failed.", regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return securityGroupNames;
    }

    private List<String> getSecurityGroupRuleList(
            String siteName, String regionName, String userId) {
        List<String> securityGroupRuleIds = new ArrayList<>();
        try {
            VpcClient vpcClient = getVpcClient(siteName, regionName, userId);
            ListSecurityGroupRulesRequest request = new ListSecurityGroupRulesRequest();
            ListSecurityGroupRulesResponse response =
                    vpcClient
                            .listSecurityGroupRulesInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                securityGroupRuleIds =
                        response.getSecurityGroupRules().stream()
                                .map(SecurityGroupRule::getId)
                                .toList();
            }
        } catch (Exception e) {
            log.error(
                    "FlexibleEngineClient listSecurityGroupRules with region {} failed.",
                    regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return securityGroupRuleIds;
    }

    private List<String> getPublicIpList(String siteName, String regionName, String userId) {
        List<String> publicIpAddresses = new ArrayList<>();
        try {
            EipClient eipClient = getEipClient(siteName, regionName, userId);
            ListPublicipsRequest request = new ListPublicipsRequest();
            ListPublicipsResponse response =
                    eipClient
                            .listPublicipsInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                publicIpAddresses =
                        response.getPublicips().stream()
                                .map(PublicipShowResp::getPublicIpAddress)
                                .toList();
            }
        } catch (Exception e) {
            log.error("FlexibleEngineClient listPublicIps with region {} failed.", regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return publicIpAddresses;
    }

    private List<String> getVolumeList(String siteName, String regionName, String userId) {
        List<String> volumeNames = new ArrayList<>();
        try {
            EvsClient evsClient = getEvsClient(siteName, regionName, userId);
            ListVolumesRequest request = new ListVolumesRequest();
            ListVolumesResponse response =
                    evsClient
                            .listVolumesInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                volumeNames = response.getVolumes().stream().map(VolumeDetail::getName).toList();
            }
        } catch (Exception e) {
            log.error("FlexibleEngineClient listVolumes with region {} failed.", regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return volumeNames;
    }

    private List<String> getKeyPairsList(String siteName, String regionName, String userId) {
        List<String> keyPairNames = new ArrayList<>();
        try {
            EcsClient ecsClient = getEcsClient(siteName, regionName, userId);
            NovaListKeypairsRequest request = new NovaListKeypairsRequest();
            NovaListKeypairsResponse response =
                    ecsClient
                            .novaListKeypairsInvoker(request)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (response.getHttpStatusCode() == 200) {
                keyPairNames =
                        response.getKeypairs().stream()
                                .map(NovaListKeypairsResult::getKeypair)
                                .map(NovaSimpleKeypair::getName)
                                .toList();
            }
        } catch (Exception e) {
            log.error("FlexibleEngineClient listKeyPairs with region {} failed.", regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        return keyPairNames;
    }

    private EcsClient getEcsClient(String siteName, String regionName, String userId) {
        ICredential credential = getCredential(siteName, userId);
        return flexibleEngineClient.getEcsClient(credential, regionName);
    }

    private VpcClient getVpcClient(String siteName, String regionName, String userId) {
        ICredential credential = getCredential(siteName, userId);
        return flexibleEngineClient.getVpcClient(credential, regionName);
    }

    private EipClient getEipClient(String siteName, String regionName, String userId) {
        ICredential credential = getCredential(siteName, userId);
        return flexibleEngineClient.getEipClient(credential, regionName);
    }

    private EvsClient getEvsClient(String siteName, String regionName, String userId) {
        ICredential credential = getCredential(siteName, userId);
        return flexibleEngineClient.getEvsClient(credential, regionName);
    }

    private ICredential getCredential(String siteName, String userId) {
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(
                        Csp.FLEXIBLE_ENGINE, siteName, CredentialType.VARIABLES, userId);
        return flexibleEngineClient.getCredential(credential);
    }

    /** get resources name for service deployment. */
    public Map<String, String> getComputeResourcesInServiceDeployment(File scriptFile) {
        Map<String, Object> results;
        Map<String, String> resources = new HashMap<>();
        try {
            results = new HCLParser().parse(scriptFile, "UTF-8");
            if (!CollectionUtils.isEmpty(results) && results.containsKey(RESOURCE)) {
                Map<String, Object> resourceMap = (Map<String, Object>) results.get(RESOURCE);
                if (!CollectionUtils.isEmpty(resourceMap)
                        && resourceMap.containsKey(FLEXIBLE_ENGINE_COMPUTE_INSTANCE)) {
                    Map<String, Object> resourceInfoMap =
                            (Map<String, Object>) resourceMap.get(FLEXIBLE_ENGINE_COMPUTE_INSTANCE);
                    if (!CollectionUtils.isEmpty(resourceInfoMap)) {
                        Set<String> resourceNameSet = resourceInfoMap.keySet();
                        resourceNameSet.forEach(
                                resourceName -> {
                                    resources.put(resourceName, FLEXIBLE_ENGINE_COMPUTE_INSTANCE);
                                });
                    }
                }
            }
        } catch (HCLParserException | IOException e) {
            String error =
                    String.format(
                            "Hcl4j parse terraform.tf file error, error %s .", e.getMessage());
            log.error(error);
            throw new TerraformScriptFormatInvalidException(List.of(error));
        }
        return resources;
    }
}
