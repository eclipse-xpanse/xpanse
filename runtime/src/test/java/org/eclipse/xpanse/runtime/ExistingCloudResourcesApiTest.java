package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsRequest;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsResponse;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsResult;
import com.huaweicloud.sdk.ecs.v2.model.NovaSimpleKeypair;
import com.huaweicloud.sdk.eip.v2.model.ListPublicipsRequest;
import com.huaweicloud.sdk.eip.v2.model.ListPublicipsResponse;
import com.huaweicloud.sdk.eip.v2.model.PublicipShowResp;
import com.huaweicloud.sdk.evs.v2.model.ListVolumesRequest;
import com.huaweicloud.sdk.evs.v2.model.ListVolumesResponse;
import com.huaweicloud.sdk.evs.v2.model.VolumeDetail;
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
import jakarta.transaction.Transactional;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.compute.domain.NovaKeypair;
import org.openstack4j.openstack.compute.domain.NovaServer;
import org.openstack4j.openstack.networking.domain.NeutronFloatingIP;
import org.openstack4j.openstack.networking.domain.NeutronNetwork;
import org.openstack4j.openstack.networking.domain.NeutronSecurityGroup;
import org.openstack4j.openstack.networking.domain.NeutronSecurityGroupRule;
import org.openstack4j.openstack.networking.domain.NeutronSubnet;
import org.openstack4j.openstack.storage.block.domain.CinderVolume;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SuppressWarnings("unchecked")
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ExistingCloudResourcesApiTest extends ApisTestCommon {

    @BeforeEach
    void setUp() {
        if (mockOsFactory != null) {
            mockOsFactory.close();
        }
        mockOsFactory = mockStatic(OSFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockOsFactory.close();
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testExistingCloudResourcesApi() throws Exception {
        testGetExistingResourceNamesWithKindThrowsException();

        testGetExistingResourceNamesWithKindForHuaweiCloud();
        testGetExistingResourceNamesWithKindForFlexibleEngine();

        testGetExistingResourceNamesWithKindForCspBasedOpenstack();
    }

    void testGetExistingResourceNamesWithKindForHuaweiCloud() throws Exception {
        mockSdkClientsForHuaweiCloud();
        // Setup
        addCredentialForHuaweiCloud();
        Csp huawei = Csp.HUAWEI_CLOUD;
        String site = "Chinese Mainland";
        String region = "cn-southwest-2";
        ListVpcsResponse listVpcResponse = new ListVpcsResponse();
        listVpcResponse.setHttpStatusCode(200);
        listVpcResponse.setVpcs(List.of(new Vpc().withName("huawei_vpc_test")));
        mockListVpcsInvoker(listVpcResponse);
        List<String> huaweiVpcResult = List.of("huawei_vpc_test");
        // Run the test
        final MockHttpServletResponse huaweiVpcResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VPC, huawei, site, region);
        // Verify the results
        assertThat(huaweiVpcResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiVpcResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiVpcResult));

        // Setup
        ListSubnetsResponse listSubnetsResponse = new ListSubnetsResponse();
        listSubnetsResponse.setHttpStatusCode(200);
        listSubnetsResponse.setSubnets(List.of(new Subnet().withName("huawei_subnet_test")));
        mockListSubnetsInvoker(listSubnetsResponse);
        List<String> huaweiSubnetsResult = List.of("huawei_subnet_test");
        // Run the test
        final MockHttpServletResponse huaweiSubnetsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SUBNET, huawei, site, region);
        // Verify the results
        assertThat(huaweiSubnetsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiSubnetsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiSubnetsResult));

        // Setup
        ListSecurityGroupsResponse listSecurityGroupsResponse = new ListSecurityGroupsResponse();
        listSecurityGroupsResponse.setHttpStatusCode(200);
        listSecurityGroupsResponse.setSecurityGroups(
                List.of(new SecurityGroup().withName("huawei_security_group_test")));
        mockListSecurityGroupsInvoker(listSecurityGroupsResponse);
        List<String> huaweiSecurityGroupsResult = List.of("huawei_security_group_test");
        // Run the test
        final MockHttpServletResponse huaweiSecurityGroupsResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP, huawei, site, region);
        // Verify the results
        assertThat(huaweiSecurityGroupsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiSecurityGroupsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiSecurityGroupsResult));

        // Setup
        ListSecurityGroupRulesResponse listSecurityGroupRulesResponse =
                new ListSecurityGroupRulesResponse();
        listSecurityGroupRulesResponse.setHttpStatusCode(200);
        listSecurityGroupRulesResponse.setSecurityGroupRules(
                List.of(new SecurityGroupRule().withId("huawei_security_group_rule_test")));
        mockListSecurityGroupRulesInvoker(listSecurityGroupRulesResponse);
        List<String> huaweiSecurityGroupRulesResult = List.of("huawei_security_group_rule_test");
        // Run the test
        final MockHttpServletResponse huaweiSecurityGroupRulesResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP_RULE, huawei, site, region);
        // Verify the results
        assertThat(huaweiSecurityGroupRulesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiSecurityGroupRulesResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiSecurityGroupRulesResult));

        // Setup
        ListPublicipsResponse listPublicipsResponse = new ListPublicipsResponse();
        listPublicipsResponse.setHttpStatusCode(200);
        listPublicipsResponse.setPublicips(
                List.of(new PublicipShowResp().withPublicIpAddress("huawei_public_ip_test")));
        mockListPublicIpsInvoker(listPublicipsResponse);
        List<String> huaweiPublicIpsResult = List.of("huawei_public_ip_test");
        // Run the test
        final MockHttpServletResponse huaweiPublicIpsResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.PUBLIC_IP, huawei, site, region);
        // Verify the results
        assertThat(huaweiPublicIpsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiPublicIpsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiPublicIpsResult));

        // Setup
        ListVolumesResponse listVolumesResponse = new ListVolumesResponse();
        listVolumesResponse.setHttpStatusCode(200);
        listVolumesResponse.setVolumes(List.of(new VolumeDetail().withName("huawei_volume_test")));
        mockListVolumesInvoker(listVolumesResponse);
        List<String> huaweiVolumesResult = List.of("huawei_volume_test");
        // Run the test
        final MockHttpServletResponse huaweiVolumesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VOLUME, huawei, site, region);
        // Verify the results
        assertThat(huaweiVolumesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiVolumesResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiVolumesResult));

        // Setup
        NovaListKeypairsResponse listKeypairsResponse = new NovaListKeypairsResponse();
        listKeypairsResponse.setHttpStatusCode(200);
        listKeypairsResponse.setKeypairs(
                List.of(
                        new NovaListKeypairsResult()
                                .withKeypair(
                                        new NovaSimpleKeypair().withName("huawei_keypair_test"))));
        mockListKeypairsInvoker(listKeypairsResponse);
        List<String> huaweiKeypairsResult = List.of("huawei_keypair_test");
        // Run the test
        final MockHttpServletResponse huaweiKeypairsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.KEYPAIR, huawei, site, region);
        // Verify the results
        assertThat(huaweiKeypairsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiKeypairsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiKeypairsResult));

        // Setup
        List<String> huaweiVmResult = Collections.emptyList();
        // Run the test
        final MockHttpServletResponse huaweiVmResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VM, huawei, site, region);
        // Verify the results
        assertThat(huaweiVmResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiVmResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiVmResult));

        deleteCredential(Csp.HUAWEI_CLOUD, site, CredentialType.VARIABLES, "AK_SK");
    }

    void mockListVpcsInvoker(ListVpcsResponse listVpcResponse) {
        SyncInvoker<ListVpcsRequest, ListVpcsResponse> mockInvoker = mock(SyncInvoker.class);
        when(mockVpcClient.listVpcsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(listVpcResponse);
    }

    void mockListSubnetsInvoker(ListSubnetsResponse mockResponse) {
        SyncInvoker<ListSubnetsRequest, ListSubnetsResponse> mockInvoker = mock(SyncInvoker.class);
        when(mockVpcClient.listSubnetsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(mockResponse);
    }

    void mockListSecurityGroupsInvoker(ListSecurityGroupsResponse mockResponse) {
        SyncInvoker<ListSecurityGroupsRequest, ListSecurityGroupsResponse> mockInvoker =
                mock(SyncInvoker.class);
        when(mockVpcClient.listSecurityGroupsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(mockResponse);
    }

    void mockListSecurityGroupRulesInvoker(ListSecurityGroupRulesResponse mockResponse) {
        SyncInvoker<ListSecurityGroupRulesRequest, ListSecurityGroupRulesResponse> mockInvoker =
                mock(SyncInvoker.class);
        when(mockVpcClient.listSecurityGroupRulesInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(mockResponse);
    }

    void mockListPublicIpsInvoker(ListPublicipsResponse mockResponse) {
        SyncInvoker<ListPublicipsRequest, ListPublicipsResponse> mockInvoker =
                mock(SyncInvoker.class);
        when(mockEipClient.listPublicipsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(mockResponse);
    }

    void mockListVolumesInvoker(ListVolumesResponse mockResponse) {
        SyncInvoker<ListVolumesRequest, ListVolumesResponse> mockInvoker = mock(SyncInvoker.class);
        when(mockEvsClient.listVolumesInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(mockResponse);
    }

    void mockListKeypairsInvoker(NovaListKeypairsResponse mockResponse) {
        SyncInvoker<NovaListKeypairsRequest, NovaListKeypairsResponse> mockInvoker =
                mock(SyncInvoker.class);
        when(mockEcsClient.novaListKeypairsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(mockResponse);
    }

    void testGetExistingResourceNamesWithKindForFlexibleEngine() throws Exception {
        mockSdkClientsForFlexibleEngine();
        addCredentialForFlexibleEngine();
        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        String site = "default";
        String region = "eu-west-0";
        ListVpcsResponse listVpcResponse = new ListVpcsResponse();
        listVpcResponse.setHttpStatusCode(200);
        listVpcResponse.setVpcs(List.of(new Vpc().withName("flexibleEngine_vpc_test")));
        mockListVpcsInvoker(listVpcResponse);
        List<String> flexibleEngineVpcResult = List.of("flexibleEngine_vpc_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineVpcResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.VPC, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEngineVpcResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineVpcResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineVpcResult));

        // Setup
        ListSubnetsResponse listSubnetsResponse = new ListSubnetsResponse();
        listSubnetsResponse.setHttpStatusCode(200);
        listSubnetsResponse.setSubnets(
                List.of(new Subnet().withName("flexibleEngine_subnet_test")));
        mockListSubnetsInvoker(listSubnetsResponse);
        List<String> flexibleEngineSubnetsResult = List.of("flexibleEngine_subnet_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineSubnetsResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SUBNET, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEngineSubnetsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineSubnetsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineSubnetsResult));

        // Setup
        ListSecurityGroupsResponse listSecurityGroupsResponse = new ListSecurityGroupsResponse();
        listSecurityGroupsResponse.setHttpStatusCode(200);
        listSecurityGroupsResponse.setSecurityGroups(
                List.of(new SecurityGroup().withName("flexibleEngine_security_group_test")));
        mockListSecurityGroupsInvoker(listSecurityGroupsResponse);
        List<String> flexibleEngineSecurityGroupsResult =
                List.of("flexibleEngine_security_group_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineSecurityGroupsResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEngineSecurityGroupsResponse.getStatus())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineSecurityGroupsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineSecurityGroupsResult));

        // Setup
        ListSecurityGroupRulesResponse listSecurityGroupRulesResponse =
                new ListSecurityGroupRulesResponse();
        listSecurityGroupRulesResponse.setHttpStatusCode(200);
        listSecurityGroupRulesResponse.setSecurityGroupRules(
                List.of(new SecurityGroupRule().withId("flexibleEngine_security_group_rule_test")));
        mockListSecurityGroupRulesInvoker(listSecurityGroupRulesResponse);
        List<String> flexibleEngineSecurityGroupRulesResult =
                List.of("flexibleEngine_security_group_rule_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineSecurityGroupRulesResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP_RULE, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEngineSecurityGroupRulesResponse.getStatus())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineSecurityGroupRulesResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineSecurityGroupRulesResult));

        // Setup
        ListPublicipsResponse listPublicipsResponse = new ListPublicipsResponse();
        listPublicipsResponse.setHttpStatusCode(200);
        listPublicipsResponse.setPublicips(
                List.of(
                        new PublicipShowResp()
                                .withPublicIpAddress("flexibleEngine_public_ip_test")));
        mockListPublicIpsInvoker(listPublicipsResponse);
        List<String> flexibleEnginePublicIpsResult = List.of("flexibleEngine_public_ip_test");
        // Run the test
        final MockHttpServletResponse flexibleEnginePublicIpsResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.PUBLIC_IP, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEnginePublicIpsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEnginePublicIpsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEnginePublicIpsResult));

        // Setup
        ListVolumesResponse listVolumesResponse = new ListVolumesResponse();
        listVolumesResponse.setHttpStatusCode(200);
        listVolumesResponse.setVolumes(
                List.of(new VolumeDetail().withName("flexibleEngine_volume_test")));
        mockListVolumesInvoker(listVolumesResponse);
        List<String> flexibleEngineVolumesResult = List.of("flexibleEngine_volume_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineVolumesResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.VOLUME, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEngineVolumesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineVolumesResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineVolumesResult));

        // Setup
        NovaListKeypairsResponse listKeypairsResponse = new NovaListKeypairsResponse();
        listKeypairsResponse.setHttpStatusCode(200);
        listKeypairsResponse.setKeypairs(
                List.of(
                        new NovaListKeypairsResult()
                                .withKeypair(
                                        new NovaSimpleKeypair()
                                                .withName("flexibleEngine_keypair_test"))));
        mockListKeypairsInvoker(listKeypairsResponse);
        List<String> flexibleEngineKeypairsResult = List.of("flexibleEngine_keypair_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineKeypairsResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.KEYPAIR, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEngineKeypairsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineKeypairsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineKeypairsResult));

        // Setup
        List<String> flexibleEngineVmResult = Collections.emptyList();
        // Run the test
        final MockHttpServletResponse flexibleEngineVmResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.VM, flexibleEngine, site, region);
        // Verify the results
        assertThat(flexibleEngineVmResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineVmResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineVmResult));

        deleteCredential(Csp.FLEXIBLE_ENGINE, site, CredentialType.VARIABLES, "AK_SK");
    }

    void testGetExistingResourceNamesWithKindForCspBasedOpenstack() throws Exception {
        testGetExistingResourceNamesWithKindForCspWithOsClient(Csp.OPENSTACK_TESTLAB);
        testGetExistingResourceNamesWithKindForCspWithOsClient(Csp.PLUS_SERVER);
        testGetExistingResourceNamesWithKindForCspWithOsClient(Csp.REGIO_CLOUD);
    }

    void testGetExistingResourceNamesWithKindThrowsException() throws Exception {
        getExistingResourceNamesWithKindThrowsClientApiCallFailedException(
                Csp.HUAWEI_CLOUD, "International", "cn-southwest-2");
        getExistingResourceNamesWithKindThrowsClientApiCallFailedException(
                Csp.FLEXIBLE_ENGINE, "default", "eu-west-0");
        getExistingResourceNamesWithKindThrowsClientApiCallFailedException(
                Csp.OPENSTACK_TESTLAB, "default", "RegionOne");
        getExistingResourceNamesWithKindThrowsClientApiCallFailedException(
                Csp.PLUS_SERVER, "default", "RegionOne");
        getExistingResourceNamesWithKindThrowsClientApiCallFailedException(
                Csp.REGIO_CLOUD, "default", "RegionOne");
    }

    void getExistingResourceNamesWithKindThrowsClientApiCallFailedException(
            Csp csp, String site, String region) throws Exception {
        final MockHttpServletResponse listVpcResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VPC, csp, site, region);
        ErrorResponse vpcErrorResponse =
                objectMapper.readValue(listVpcResponse.getContentAsString(), ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY.value(), listVpcResponse.getStatus());
        Assertions.assertEquals(ErrorType.BACKEND_FAILURE, vpcErrorResponse.getErrorType());

        final MockHttpServletResponse listSubnetResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SUBNET, csp, site, region);
        ErrorResponse subnetErrorResponse =
                objectMapper.readValue(
                        listSubnetResponse.getContentAsString(), ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY.value(), listSubnetResponse.getStatus());
        Assertions.assertEquals(ErrorType.BACKEND_FAILURE, subnetErrorResponse.getErrorType());

        final MockHttpServletResponse listSecurityGroupResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP, csp, site, region);
        ErrorResponse securityGroupErrorResponse =
                objectMapper.readValue(
                        listSecurityGroupResponse.getContentAsString(), ErrorResponse.class);
        Assertions.assertEquals(
                HttpStatus.BAD_GATEWAY.value(), listSecurityGroupResponse.getStatus());
        Assertions.assertEquals(
                ErrorType.BACKEND_FAILURE, securityGroupErrorResponse.getErrorType());

        final MockHttpServletResponse listSecurityGroupRuleResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP_RULE, csp, site, region);
        ErrorResponse securityGroupRuleErrorResponse =
                objectMapper.readValue(
                        listSecurityGroupRuleResponse.getContentAsString(), ErrorResponse.class);
        Assertions.assertEquals(
                HttpStatus.BAD_GATEWAY.value(), listSecurityGroupRuleResponse.getStatus());
        Assertions.assertEquals(
                ErrorType.BACKEND_FAILURE, securityGroupRuleErrorResponse.getErrorType());

        final MockHttpServletResponse listPublicIpResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.PUBLIC_IP, csp, site, region);
        ErrorResponse publicIpErrorResponse =
                objectMapper.readValue(
                        listPublicIpResponse.getContentAsString(), ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY.value(), listPublicIpResponse.getStatus());
        Assertions.assertEquals(ErrorType.BACKEND_FAILURE, publicIpErrorResponse.getErrorType());

        final MockHttpServletResponse listVolumeResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VOLUME, csp, site, region);
        ErrorResponse volumeErrorResponse =
                objectMapper.readValue(
                        listVolumeResponse.getContentAsString(), ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY.value(), listVolumeResponse.getStatus());
        Assertions.assertEquals(ErrorType.BACKEND_FAILURE, volumeErrorResponse.getErrorType());

        final MockHttpServletResponse listKeypairResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.KEYPAIR, csp, site, region);
        ErrorResponse keypairErrorResponse =
                objectMapper.readValue(
                        listKeypairResponse.getContentAsString(), ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY.value(), listKeypairResponse.getStatus());
        Assertions.assertEquals(ErrorType.BACKEND_FAILURE, keypairErrorResponse.getErrorType());
    }

    void testGetExistingResourceNamesWithKindForCspWithOsClient(Csp csp) throws Exception {
        // Setup
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();
        addCredentialForOpenstack(csp);
        String site = "default";
        String region = "RegionOne";
        List<String> networksResult = List.of("network_test");
        File networksjonFile = new File("src/test/resources/openstack/network/networks.json");
        NeutronNetwork.Networks networksResponse =
                objectMapper.readValue(networksjonFile, NeutronNetwork.Networks.class);
        when((List<NeutronNetwork>) mockOsClient.networking().network().list())
                .thenReturn(networksResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackVpcResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VPC, csp, site, region);
        // Verify the results
        assertThat(openstackVpcResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackVpcResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(networksResult));

        List<String> subnetsResult = List.of("subnet_test");
        File subnetsjonFile = new File("src/test/resources/openstack/network/subnets.json");
        NeutronSubnet.Subnets subnetsResponse =
                objectMapper.readValue(subnetsjonFile, NeutronSubnet.Subnets.class);
        when((List<NeutronSubnet>) mockOsClient.networking().subnet().list())
                .thenReturn(subnetsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackSubnetsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SUBNET, csp, site, region);
        // Verify the results
        assertThat(openstackSubnetsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackSubnetsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(subnetsResult));

        List<String> secGroupsResult = List.of("security_group_test");
        File secGroupsjonFile =
                new File("src/test/resources/openstack/network/security_groups.json");
        NeutronSecurityGroup.SecurityGroups secGroupsResponse =
                objectMapper.readValue(secGroupsjonFile, NeutronSecurityGroup.SecurityGroups.class);
        when((List<NeutronSecurityGroup>) mockOsClient.networking().securitygroup().list())
                .thenReturn(secGroupsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackSecurityGroupsResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP, csp, site, region);
        // Verify the results
        assertThat(openstackSecurityGroupsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackSecurityGroupsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(secGroupsResult));

        List<String> secGroupRulesResult = List.of("security_group_rule_test_id");
        File secGroupRulesjonFile =
                new File("src/test/resources/openstack/network/security_group_rules.json");
        NeutronSecurityGroupRule.SecurityGroupRules secGroupRulesResponse =
                objectMapper.readValue(
                        secGroupRulesjonFile, NeutronSecurityGroupRule.SecurityGroupRules.class);
        when((List<NeutronSecurityGroupRule>) mockOsClient.networking().securityrule().list())
                .thenReturn(secGroupRulesResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackSecurityGroupRulesResponse =
                getExistingResourceNamesWithKind(
                        DeployResourceKind.SECURITY_GROUP_RULE, csp, site, region);
        // Verify the results
        assertThat(openstackSecurityGroupRulesResponse.getStatus())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(openstackSecurityGroupRulesResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(secGroupRulesResult));

        List<String> publicIpsResult = List.of("10.0.50.29");
        File publicIpsjonFile =
                new File("src/test/resources/openstack/network/network_fips_list.json");
        NeutronFloatingIP.FloatingIPs publicIpsResponse =
                objectMapper.readValue(publicIpsjonFile, NeutronFloatingIP.FloatingIPs.class);
        when((List<NeutronFloatingIP>) mockOsClient.networking().floatingip().list())
                .thenReturn(publicIpsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackPublicIpsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.PUBLIC_IP, csp, site, region);
        // Verify the results
        assertThat(openstackPublicIpsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackPublicIpsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(publicIpsResult));

        List<String> volumesResult = List.of("volume_test");
        File volumesjonFile = new File("src/test/resources/openstack/storage/volumes.json");
        CinderVolume.Volumes volumesResponse =
                objectMapper.readValue(volumesjonFile, CinderVolume.Volumes.class);
        when((List<CinderVolume>) mockOsClient.blockStorage().volumes().list())
                .thenReturn(volumesResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackVolumesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VOLUME, csp, site, region);
        // Verify the results
        assertThat(openstackVolumesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackVolumesResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(volumesResult));

        File keypairsjonFile = new File("src/test/resources/openstack/compute/keypairs.json");
        NovaKeypair.Keypairs keypairsResponse =
                objectMapper.readValue(keypairsjonFile, NovaKeypair.Keypairs.class);
        when((List<NovaKeypair>) mockOsClient.compute().keypairs().list())
                .thenReturn(keypairsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackKeypairsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.KEYPAIR, csp, site, region);
        // Verify the results
        assertThat(openstackKeypairsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackKeypairsResponse.getContentAsString()).isEqualTo("[]");

        File serversjonFile = new File("src/test/resources/openstack/compute/servers.json");
        NovaServer.Servers serversResponse =
                objectMapper.readValue(serversjonFile, NovaServer.Servers.class);
        when((List<NovaServer>) mockOsClient.compute().servers().list())
                .thenReturn(serversResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackVmResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VM, csp, site, region);
        // Verify the results
        assertThat(openstackVmResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackVmResponse.getContentAsString()).isEqualTo("[]");

        deleteCredential(csp, site, CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }

    private MockHttpServletResponse getExistingResourceNamesWithKind(
            DeployResourceKind kind, Csp csp, String siteName, String regionName) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/csp/resources/{deployResourceKind}", kind)
                                .param("csp", csp.toValue())
                                .param("siteName", siteName)
                                .param("regionName", regionName)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
