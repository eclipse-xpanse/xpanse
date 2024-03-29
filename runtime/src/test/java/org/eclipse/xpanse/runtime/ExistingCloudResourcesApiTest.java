package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsResponse;
import com.huaweicloud.sdk.ecs.v2.model.NovaListKeypairsResult;
import com.huaweicloud.sdk.ecs.v2.model.NovaSimpleKeypair;
import com.huaweicloud.sdk.eip.v2.model.ListPublicipsResponse;
import com.huaweicloud.sdk.eip.v2.model.PublicipShowResp;
import com.huaweicloud.sdk.evs.v2.model.ListVolumesResponse;
import com.huaweicloud.sdk.evs.v2.model.VolumeDetail;
import com.huaweicloud.sdk.vpc.v2.model.ListSecurityGroupRulesResponse;
import com.huaweicloud.sdk.vpc.v2.model.ListSecurityGroupsResponse;
import com.huaweicloud.sdk.vpc.v2.model.ListSubnetsResponse;
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
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.AfterEach;
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

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed",
        "OS_AUTH_URL=http://127.0.0.1/v3/identity"})
@AutoConfigureMockMvc
class ExistingCloudResourcesApiTest extends ApisTestCommon {


    @BeforeEach
    void setUp() {
        mockOsFactory = mockStatic(OSFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockOsFactory.close();
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetExistingResourceNamesWithKindForHuaweiCloud() throws Exception {
        mockSdkClientsForHuaweiCloud();
        // Setup
        addCredentialForHuaweiCloud();
        Csp huawei = Csp.HUAWEI;
        String huaweiRegion = "cn-southwest-2";
        ListVpcsResponse listVpcResponse = new ListVpcsResponse();
        listVpcResponse.setHttpStatusCode(200);
        listVpcResponse.setVpcs(List.of(new Vpc().withName("huawei_vpc_test")));
        when(mockVpcClient.listVpcs(any())).thenReturn(listVpcResponse);
        List<String> huaweiVpcResult = List.of("huawei_vpc_test");
        // Run the test
        final MockHttpServletResponse huaweiVpcResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VPC, huawei, huaweiRegion);
        // Verify the results
        assertThat(huaweiVpcResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiVpcResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiVpcResult));

        // Setup
        ListSubnetsResponse listSubnetsResponse = new ListSubnetsResponse();
        listSubnetsResponse.setHttpStatusCode(200);
        listSubnetsResponse.setSubnets(List.of(new Subnet().withName("huawei_subnet_test")));
        when(mockVpcClient.listSubnets(any())).thenReturn(listSubnetsResponse);
        List<String> huaweiSubnetsResult = List.of("huawei_subnet_test");
        // Run the test
        final MockHttpServletResponse huaweiSubnetsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SUBNET, huawei, huaweiRegion);
        // Verify the results
        assertThat(huaweiSubnetsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiSubnetsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiSubnetsResult));

        // Setup
        ListSecurityGroupsResponse listSecurityGroupsResponse = new ListSecurityGroupsResponse();
        listSecurityGroupsResponse.setHttpStatusCode(200);
        listSecurityGroupsResponse.setSecurityGroups(
                List.of(new SecurityGroup().withName("huawei_security_group_test")));
        when(mockVpcClient.listSecurityGroups(any())).thenReturn(listSecurityGroupsResponse);
        List<String> huaweiSecurityGroupsResult = List.of("huawei_security_group_test");
        // Run the test
        final MockHttpServletResponse huaweiSecurityGroupsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP, huawei,
                        huaweiRegion);
        // Verify the results
        assertThat(huaweiSecurityGroupsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiSecurityGroupsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiSecurityGroupsResult));

        // Setup
        ListSecurityGroupRulesResponse listSecurityGroupRulesResponse =
                new ListSecurityGroupRulesResponse();
        listSecurityGroupRulesResponse.setHttpStatusCode(200);
        listSecurityGroupRulesResponse.setSecurityGroupRules(
                List.of(new SecurityGroupRule().withId("huawei_security_group_rule_test")));
        when(mockVpcClient.listSecurityGroupRules(any())).thenReturn(
                listSecurityGroupRulesResponse);
        List<String> huaweiSecurityGroupRulesResult = List.of("huawei_security_group_rule_test");
        // Run the test
        final MockHttpServletResponse huaweiSecurityGroupRulesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP_RULE, huawei,
                        huaweiRegion);
        // Verify the results
        assertThat(huaweiSecurityGroupRulesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiSecurityGroupRulesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiSecurityGroupRulesResult));

        // Setup
        ListPublicipsResponse listPublicipsResponse = new ListPublicipsResponse();
        listPublicipsResponse.setHttpStatusCode(200);
        listPublicipsResponse.setPublicips(
                List.of(new PublicipShowResp().withPublicIpAddress("huawei_public_ip_test")));
        when(mockEipClient.listPublicips(any())).thenReturn(listPublicipsResponse);
        List<String> huaweiPublicIpsResult = List.of("huawei_public_ip_test");
        // Run the test
        final MockHttpServletResponse huaweiPublicIpsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.PUBLIC_IP, huawei,
                        huaweiRegion);
        // Verify the results
        assertThat(huaweiPublicIpsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiPublicIpsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiPublicIpsResult));

        // Setup
        ListVolumesResponse listVolumesResponse = new ListVolumesResponse();
        listVolumesResponse.setHttpStatusCode(200);
        listVolumesResponse.setVolumes(List.of(new VolumeDetail().withName("huawei_volume_test")));
        when(mockEvsClient.listVolumes(any())).thenReturn(listVolumesResponse);
        List<String> huaweiVolumesResult = List.of("huawei_volume_test");
        // Run the test
        final MockHttpServletResponse huaweiVolumesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VOLUME, huawei, huaweiRegion);
        // Verify the results
        assertThat(huaweiVolumesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiVolumesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiVolumesResult));

        // Setup
        NovaListKeypairsResponse listKeypairsResponse = new NovaListKeypairsResponse();
        listKeypairsResponse.setHttpStatusCode(200);
        listKeypairsResponse.setKeypairs(List.of(new NovaListKeypairsResult().withKeypair(
                new NovaSimpleKeypair().withName("huawei_keypair_test"))));
        when(mockEcsClient.novaListKeypairs(any())).thenReturn(listKeypairsResponse);
        List<String> huaweiKeypairsResult = List.of("huawei_keypair_test");
        // Run the test
        final MockHttpServletResponse huaweiKeypairsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.KEYPAIR, huawei, huaweiRegion);
        // Verify the results
        assertThat(huaweiKeypairsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiKeypairsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiKeypairsResult));

        // Setup
        List<String> huaweiVmResult = Collections.emptyList();
        // Run the test
        final MockHttpServletResponse huaweiVmResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VM, huawei, huaweiRegion);
        // Verify the results
        assertThat(huaweiVmResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiVmResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiVmResult));

        deleteCredential(Csp.HUAWEI, CredentialType.VARIABLES, "AK_SK");
    }


    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetExistingResourceNamesWithKindForFlexibleEngine() throws Exception {
        mockSdkClientsForFlexibleEngine();
        addCredentialForFlexibleEngine();
        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        String flexibleEngineRegion = "eu-west-0";
        ListVpcsResponse listVpcResponse = new ListVpcsResponse();
        listVpcResponse.setHttpStatusCode(200);
        listVpcResponse.setVpcs(List.of(new Vpc().withName("flexibleEngine_vpc_test")));
        when(mockVpcClient.listVpcs(any())).thenReturn(listVpcResponse);
        List<String> flexibleEngineVpcResult = List.of("flexibleEngine_vpc_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineVpcResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VPC, flexibleEngine,
                        flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEngineVpcResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineVpcResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineVpcResult));

        // Setup
        ListSubnetsResponse listSubnetsResponse = new ListSubnetsResponse();
        listSubnetsResponse.setHttpStatusCode(200);
        listSubnetsResponse.setSubnets(
                List.of(new Subnet().withName("flexibleEngine_subnet_test")));
        when(mockVpcClient.listSubnets(any())).thenReturn(listSubnetsResponse);
        List<String> flexibleEngineSubnetsResult = List.of("flexibleEngine_subnet_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineSubnetsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SUBNET, flexibleEngine,
                        flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEngineSubnetsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineSubnetsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineSubnetsResult));

        // Setup
        ListSecurityGroupsResponse listSecurityGroupsResponse = new ListSecurityGroupsResponse();
        listSecurityGroupsResponse.setHttpStatusCode(200);
        listSecurityGroupsResponse.setSecurityGroups(
                List.of(new SecurityGroup().withName("flexibleEngine_security_group_test")));
        when(mockVpcClient.listSecurityGroups(any())).thenReturn(listSecurityGroupsResponse);
        List<String> flexibleEngineSecurityGroupsResult =
                List.of("flexibleEngine_security_group_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineSecurityGroupsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP, flexibleEngine,
                        flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEngineSecurityGroupsResponse.getStatus()).isEqualTo(
                HttpStatus.OK.value());
        assertThat(flexibleEngineSecurityGroupsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineSecurityGroupsResult));

        // Setup
        ListSecurityGroupRulesResponse listSecurityGroupRulesResponse =
                new ListSecurityGroupRulesResponse();
        listSecurityGroupRulesResponse.setHttpStatusCode(200);
        listSecurityGroupRulesResponse.setSecurityGroupRules(
                List.of(new SecurityGroupRule().withId("flexibleEngine_security_group_rule_test")));
        when(mockVpcClient.listSecurityGroupRules(any())).thenReturn(
                listSecurityGroupRulesResponse);
        List<String> flexibleEngineSecurityGroupRulesResult =
                List.of("flexibleEngine_security_group_rule_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineSecurityGroupRulesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP_RULE,
                        flexibleEngine, flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEngineSecurityGroupRulesResponse.getStatus()).isEqualTo(
                HttpStatus.OK.value());
        assertThat(flexibleEngineSecurityGroupRulesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineSecurityGroupRulesResult));

        // Setup
        ListPublicipsResponse listPublicipsResponse = new ListPublicipsResponse();
        listPublicipsResponse.setHttpStatusCode(200);
        listPublicipsResponse.setPublicips(List.of(new PublicipShowResp().withPublicIpAddress(
                "flexibleEngine_public_ip_test")));
        when(mockEipClient.listPublicips(any())).thenReturn(listPublicipsResponse);
        List<String> flexibleEnginePublicIpsResult = List.of("flexibleEngine_public_ip_test");
        // Run the test
        final MockHttpServletResponse flexibleEnginePublicIpsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.PUBLIC_IP, flexibleEngine,
                        flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEnginePublicIpsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEnginePublicIpsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEnginePublicIpsResult));

        // Setup
        ListVolumesResponse listVolumesResponse = new ListVolumesResponse();
        listVolumesResponse.setHttpStatusCode(200);
        listVolumesResponse.setVolumes(
                List.of(new VolumeDetail().withName("flexibleEngine_volume_test")));
        when(mockEvsClient.listVolumes(any())).thenReturn(listVolumesResponse);
        List<String> flexibleEngineVolumesResult = List.of("flexibleEngine_volume_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineVolumesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VOLUME, flexibleEngine,
                        flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEngineVolumesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineVolumesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineVolumesResult));

        // Setup
        NovaListKeypairsResponse listKeypairsResponse = new NovaListKeypairsResponse();
        listKeypairsResponse.setHttpStatusCode(200);
        listKeypairsResponse.setKeypairs(List.of(new NovaListKeypairsResult().withKeypair(
                new NovaSimpleKeypair().withName("flexibleEngine_keypair_test"))));
        when(mockEcsClient.novaListKeypairs(any())).thenReturn(listKeypairsResponse);
        List<String> flexibleEngineKeypairsResult = List.of("flexibleEngine_keypair_test");
        // Run the test
        final MockHttpServletResponse flexibleEngineKeypairsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.KEYPAIR, flexibleEngine,
                        flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEngineKeypairsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineKeypairsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineKeypairsResult));

        // Setup
        List<String> flexibleEngineVmResult = Collections.emptyList();
        // Run the test
        final MockHttpServletResponse flexibleEngineVmResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VM, flexibleEngine,
                        flexibleEngineRegion);
        // Verify the results
        assertThat(flexibleEngineVmResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineVmResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineVmResult));

        deleteCredential(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES, "AK_SK");
    }


    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetExistingResourceNamesWithKindForOpenstack() throws Exception {
        testGetExistingResourceNamesWithKindForCspWithOsClient(Csp.OPENSTACK);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetExistingResourceNamesWithKindForScs() throws Exception {
        testGetExistingResourceNamesWithKindForCspWithOsClient(Csp.SCS);
    }

    void testGetExistingResourceNamesWithKindForCspWithOsClient(Csp csp) throws Exception {
        // Setup
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();
        if (csp == Csp.OPENSTACK) {
            addCredentialForOpenstack();
        }
        if (csp == Csp.SCS) {
            addCredentialForScs();
        }
        String openstackRegion = "RegionOne";
        List<String> networksResult = List.of("network_test");
        File networksjonFile = new File("src/test/resources/openstack/network/networks.json");
        NeutronNetwork.Networks networksResponse =
                objectMapper.readValue(networksjonFile, NeutronNetwork.Networks.class);
        when((List<NeutronNetwork>) mockOsClient.networking().network().list()).thenReturn(
                networksResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackVpcResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VPC, csp, openstackRegion);
        // Verify the results
        assertThat(openstackVpcResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackVpcResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(networksResult));

        List<String> subnetsResult = List.of("subnet_test");
        File subnetsjonFile = new File("src/test/resources/openstack/network/subnets.json");
        NeutronSubnet.Subnets subnetsResponse =
                objectMapper.readValue(subnetsjonFile, NeutronSubnet.Subnets.class);
        when((List<NeutronSubnet>) mockOsClient.networking().subnet().list()).thenReturn(
                subnetsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackSubnetsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SUBNET, csp, openstackRegion);
        // Verify the results
        assertThat(openstackSubnetsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackSubnetsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(subnetsResult));

        List<String> secGroupsResult = List.of("security_group_test");
        File secGroupsjonFile =
                new File("src/test/resources/openstack/network/security_groups.json");
        NeutronSecurityGroup.SecurityGroups secGroupsResponse =
                objectMapper.readValue(secGroupsjonFile, NeutronSecurityGroup.SecurityGroups.class);
        when((List<NeutronSecurityGroup>) mockOsClient.networking().securitygroup()
                .list()).thenReturn(secGroupsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackSecurityGroupsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP, csp,
                        openstackRegion);
        // Verify the results
        assertThat(openstackSecurityGroupsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackSecurityGroupsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(secGroupsResult));

        List<String> secGroupRulesResult = List.of("security_group_rule_test_id");
        File secGroupRulesjonFile =
                new File("src/test/resources/openstack/network/security_group_rules.json");
        NeutronSecurityGroupRule.SecurityGroupRules secGroupRulesResponse =
                objectMapper.readValue(secGroupRulesjonFile,
                        NeutronSecurityGroupRule.SecurityGroupRules.class);
        when((List<NeutronSecurityGroupRule>) mockOsClient.networking().securityrule()
                .list()).thenReturn(secGroupRulesResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackSecurityGroupRulesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP_RULE, csp,
                        openstackRegion);
        // Verify the results
        assertThat(openstackSecurityGroupRulesResponse.getStatus()).isEqualTo(
                HttpStatus.OK.value());
        assertThat(openstackSecurityGroupRulesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(secGroupRulesResult));

        List<String> publicIpsResult = List.of("10.0.50.29");
        File publicIpsjonFile =
                new File("src/test/resources/openstack/network/network_fips_list.json");
        NeutronFloatingIP.FloatingIPs publicIpsResponse =
                objectMapper.readValue(publicIpsjonFile, NeutronFloatingIP.FloatingIPs.class);
        when((List<NeutronFloatingIP>) mockOsClient.networking().floatingip().list()).thenReturn(
                publicIpsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackPublicIpsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.PUBLIC_IP, csp,
                        openstackRegion);
        // Verify the results
        assertThat(openstackPublicIpsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackPublicIpsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(publicIpsResult));


        List<String> volumesResult = List.of("volume_test");
        File volumesjonFile = new File("src/test/resources/openstack/storage/volumes.json");
        CinderVolume.Volumes volumesResponse =
                objectMapper.readValue(volumesjonFile, CinderVolume.Volumes.class);
        when((List<CinderVolume>) mockOsClient.blockStorage().volumes().list()).thenReturn(
                volumesResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackVolumesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VOLUME, csp, openstackRegion);
        // Verify the results
        assertThat(openstackVolumesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackVolumesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(volumesResult));


        File keypairsjonFile = new File("src/test/resources/openstack/compute/keypairs.json");
        NovaKeypair.Keypairs keypairsResponse =
                objectMapper.readValue(keypairsjonFile, NovaKeypair.Keypairs.class);
        when((List<NovaKeypair>) mockOsClient.compute().keypairs().list()).thenReturn(
                keypairsResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackKeypairsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.KEYPAIR, csp, openstackRegion);
        // Verify the results
        assertThat(openstackKeypairsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackKeypairsResponse.getContentAsString()).isEqualTo("[]");


        File serversjonFile = new File("src/test/resources/openstack/compute/servers.json");
        NovaServer.Servers serversResponse =
                objectMapper.readValue(serversjonFile, NovaServer.Servers.class);
        when((List<NovaServer>) mockOsClient.compute().servers().list()).thenReturn(
                serversResponse.getList());
        // Run the test
        final MockHttpServletResponse openstackVmResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VM, csp, openstackRegion);
        // Verify the results
        assertThat(openstackVmResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackVmResponse.getContentAsString()).isEqualTo("[]");

        deleteCredential(csp, CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetExistingResourceNamesWithKindForFlexibleEngineReturnEmptyList() throws Exception {
        testGetExistingResourceNamesWithKindReturnEmptyList(Csp.OPENSTACK);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetExistingResourceNamesWithKindForScsReturnEmptyList() throws Exception {
        testGetExistingResourceNamesWithKindReturnEmptyList(Csp.SCS);
    }

    void testGetExistingResourceNamesWithKindReturnEmptyList(Csp csp) throws Exception {
        // Setup
        String cspRegion = "RegionOne";
        List<String> emptyResult = Collections.emptyList();
        // Run the test
        final MockHttpServletResponse cspVpcResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VPC, csp, cspRegion);
        // Verify the results
        assertThat(cspVpcResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspVpcResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

        // Run the test
        final MockHttpServletResponse cspSubnetsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SUBNET, csp, cspRegion);
        // Verify the results
        assertThat(cspSubnetsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspSubnetsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

        // Run the test
        final MockHttpServletResponse cspSecurityGroupsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP, csp, cspRegion);
        // Verify the results
        assertThat(cspSecurityGroupsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspSecurityGroupsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

        // Run the test
        final MockHttpServletResponse cspSecurityGroupRulesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.SECURITY_GROUP_RULE, csp,
                        cspRegion);
        // Verify the results
        assertThat(cspSecurityGroupRulesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspSecurityGroupRulesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

        // Run the test
        final MockHttpServletResponse cspPublicIpsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.PUBLIC_IP, csp, cspRegion);
        // Verify the results
        assertThat(cspPublicIpsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspPublicIpsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

        // Run the test
        final MockHttpServletResponse cspVolumesResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VOLUME, csp, cspRegion);
        // Verify the results
        assertThat(cspVolumesResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspVolumesResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

        // Run the test
        final MockHttpServletResponse cspKeypairsResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.KEYPAIR, csp, cspRegion);
        // Verify the results
        assertThat(cspKeypairsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspKeypairsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

        // Run the test
        final MockHttpServletResponse cspVmResponse =
                getExistingResourceNamesWithKind(DeployResourceKind.VM, csp, cspRegion);
        // Verify the results
        assertThat(cspVmResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(cspVmResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(emptyResult));

    }


    private MockHttpServletResponse getExistingResourceNamesWithKind(DeployResourceKind kind,
                                                                     Csp csp, String region)
            throws Exception {
        return mockMvc.perform(
                        get("/xpanse/csp/resources/{deployResourceKind}", kind).param("csp", csp.toValue())
                                .param("region", region).accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }
}
