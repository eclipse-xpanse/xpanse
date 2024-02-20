/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.huaweicloud.manage;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.SdkException;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
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
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Huawei Cloud Resource Manager.
 */
@Slf4j
@Component
public class HuaweiCloudResourceManager {

    private final CredentialCenter credentialCenter;
    private final HuaweiCloudClient huaweiCloudClient;

    @Autowired
    public HuaweiCloudResourceManager(
            CredentialCenter credentialCenter,
            HuaweiCloudClient huaweiCloudClient) {
        this.credentialCenter = credentialCenter;
        this.huaweiCloudClient = huaweiCloudClient;
    }

    /**
     * List HuaweiCloud resource by the kind of ReusbaleCloudResource.
     */
    public List<String> getExistingResourcesOfType(String userId,
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

    private List<String> getVpcList(String userId, String region) {
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListVpcsRequest request = new ListVpcsRequest();
            ListVpcsResponse response = vpcClient.listVpcs(request);
            if (response.getHttpStatusCode() == 200) {
                return response.getVpcs().stream().map(Vpc::getName).toList();
            }
        } catch (SdkException e) {
            log.error("Get HuaweiCloud vpc resources failed, error:{}", e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<String> getSubnetList(String userId, String region) {
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListSubnetsRequest request = new ListSubnetsRequest();
            ListSubnetsResponse response = vpcClient.listSubnets(request);
            if (response.getHttpStatusCode() == 200) {
                return response.getSubnets().stream().map(Subnet::getName).toList();
            }
        } catch (SdkException e) {
            log.error("Get HuaweiCloud Subnet resources failed, error:{}", e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<String> getSecurityGroupsList(String userId, String region) {
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListSecurityGroupsRequest request = new ListSecurityGroupsRequest();
            ListSecurityGroupsResponse response = vpcClient.listSecurityGroups(request);
            if (response.getHttpStatusCode() == 200) {
                return response.getSecurityGroups().stream().map(SecurityGroup::getName).toList();
            }
        } catch (SdkException e) {
            log.error("Get HuaweiCloud SecurityGroup resources failed, error:{}", e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<String> getSecurityGroupRuleList(String userId, String region) {
        try {
            VpcClient vpcClient = getVpcClient(userId, region);
            ListSecurityGroupRulesRequest request = new ListSecurityGroupRulesRequest();
            ListSecurityGroupRulesResponse response = vpcClient.listSecurityGroupRules(request);
            if (response.getHttpStatusCode() == 200) {
                return response.getSecurityGroupRules().stream().map(SecurityGroupRule::getId)
                        .toList();
            }
        } catch (SdkException e) {
            log.error("Get HuaweiCloud SecurityGroupRule resources failed, error:{}",
                    e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<String> getPublicIpList(String userId, String region) {
        try {
            EipClient eipClient = getEipClient(userId, region);
            ListPublicipsRequest request = new ListPublicipsRequest();
            ListPublicipsResponse response = eipClient.listPublicips(request);
            if (response.getHttpStatusCode() == 200) {
                return response.getPublicips().stream().map(PublicipShowResp::getPublicIpAddress)
                        .toList();
            }
        } catch (SdkException e) {
            log.error("Get HuaweiCloud PublicIp resources failed, error:{}", e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<String> getVolumeList(String userId, String region) {
        try {
            EvsClient evsClient = getEvsClient(userId, region);
            ListVolumesRequest request = new ListVolumesRequest();
            ListVolumesResponse response = evsClient.listVolumes(request);
            if (response.getHttpStatusCode() == 200) {
                return response.getVolumes().stream().map(VolumeDetail::getName).toList();
            }
        } catch (SdkException e) {
            log.error("Get HuaweiCloud VM resources failed, error:{}", e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<String> getKeyPairsList(String userId, String region) {
        try {
            EcsClient ecsClient = getEcsClient(userId, region);
            NovaListKeypairsRequest request = new NovaListKeypairsRequest();
            NovaListKeypairsResponse response = ecsClient.novaListKeypairs(request);
            if (response.getHttpStatusCode() == 200) {
                return response.getKeypairs().stream().map(NovaListKeypairsResult::getKeypair)
                        .map(NovaSimpleKeypair::getName).toList();
            }
        } catch (SdkException e) {
            log.error("Get HuaweiCloud KeyPairs resources failed, error:{}", e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
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
                credentialCenter.getCredential(Csp.HUAWEI, CredentialType.VARIABLES, userId);
        return huaweiCloudClient.getCredential(credential);
    }
}
