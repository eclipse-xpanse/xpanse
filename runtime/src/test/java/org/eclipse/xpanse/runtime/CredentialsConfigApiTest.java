package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.plugins.flexibleengine.FlexibleEngineOrchestratorPlugin;
import org.eclipse.xpanse.plugins.huaweicloud.HuaweiCloudOrchestratorPlugin;
import org.eclipse.xpanse.plugins.openstack.OpenstackOrchestratorPlugin;
import org.eclipse.xpanse.plugins.scs.ScsOrchestratorPlugin;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class CredentialsConfigApiTest extends ApisTestCommon {

    @Resource
    private HuaweiCloudOrchestratorPlugin huaweiCloudPlugin;
    @Resource
    private FlexibleEngineOrchestratorPlugin flexibleEnginePlugin;
    @Resource
    private OpenstackOrchestratorPlugin openstackPlugin;
    @Resource
    private ScsOrchestratorPlugin scsPlugin;
    @Resource
    private CredentialCenter credentialsCenter;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetCredentialTypes() throws Exception {
        // Setup
        Csp huawei = Csp.HUAWEI;
        List<CredentialType> huaweiCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse huaweiResponse = getCredentialTypes(huawei);
        // Verify the results
        assertThat(huaweiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiCredentialTypes));


        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        List<CredentialType> flexibleEngineCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse flexibleEngineResponse = getCredentialTypes(flexibleEngine);
        // Verify the results
        assertThat(flexibleEngineResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineCredentialTypes));

        // Setup
        Csp openstack = Csp.OPENSTACK;
        List<CredentialType> openstackCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse openstackResponse = getCredentialTypes(openstack);
        // Verify the results
        assertThat(openstackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(openstackCredentialTypes));

        // Setup
        Csp scs = Csp.SCS;
        List<CredentialType> scsCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse scsResponse = getCredentialTypes(scs);
        // Verify the results
        assertThat(scsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(scsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(scsCredentialTypes));

        // Setup
        Csp aws = Csp.AWS;
        Response awsResult = Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(
                        String.format("Can't find suitable plugin for the Csp %s", aws)));
        // Run the test
        final MockHttpServletResponse awsResponse = getCredentialTypes(aws);
        // Verify the results
        assertThat(awsResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(awsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(awsResult));
    }

    MockHttpServletResponse getCredentialTypes(Csp csp) throws Exception {
        return mockMvc.perform(get("/xpanse/credential_types")
                        .param("cspName", csp.toValue())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }


    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetCredentialCapabilities() throws Exception {
        CredentialType variablesType = CredentialType.VARIABLES;
        CredentialType httpType = CredentialType.HTTP_AUTHENTICATION;
        // Setup
        Csp huawei = Csp.HUAWEI;
        List<AbstractCredentialInfo> huaweiResult = getCredentialCapabilities(huawei);
        // Run the test
        final MockHttpServletResponse huaweiResponse = getCredentialCapabilities(huawei,
                variablesType);
        // Verify the results
        assertThat(huaweiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiResult));

        // Run the test
        final MockHttpServletResponse huaweiResponse1 = getCredentialCapabilities(huawei,
                httpType);
        // Verify the results
        assertThat(huaweiResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        List<AbstractCredentialInfo> flexibleEngineResult =
                getCredentialCapabilities(flexibleEngine);
        // Run the test
        final MockHttpServletResponse flexibleEngineResponse =
                getCredentialCapabilities(flexibleEngine,
                        variablesType);
        // Verify the results
        assertThat(flexibleEngineResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineResult));

        // Run the test
        final MockHttpServletResponse flexibleEngineResponse1 =
                getCredentialCapabilities(flexibleEngine,
                        httpType);
        // Verify the results
        assertThat(flexibleEngineResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp openstack = Csp.OPENSTACK;
        List<AbstractCredentialInfo> openstackResult = getCredentialCapabilities(openstack);
        // Run the test
        final MockHttpServletResponse openstackResponse = getCredentialCapabilities(openstack,
                variablesType);
        // Verify the results
        assertThat(openstackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(openstackResult));

        // Run the test
        final MockHttpServletResponse openstackResponse1 = getCredentialCapabilities(openstack,
                httpType);
        // Verify the results
        assertThat(openstackResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp scs = Csp.SCS;
        List<AbstractCredentialInfo> scsResult = getCredentialCapabilities(scs);
        // Run the test
        final MockHttpServletResponse scsResponse = getCredentialCapabilities(scs,
                variablesType);
        // Verify the results
        assertThat(scsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(scsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(scsResult));
        // Run the test
        final MockHttpServletResponse scsResponse1 = getCredentialCapabilities(scs,
                httpType);
        // Verify the results
        assertThat(scsResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(scsResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp alicloud = Csp.ALICLOUD;
        Response alicloudResult = Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(
                        String.format("Can't find suitable plugin for the Csp %s", alicloud)));
        // Run the test
        final MockHttpServletResponse alicloudResponse =
                getCredentialCapabilities(alicloud, variablesType);
        // Verify the results
        assertThat(alicloudResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(alicloudResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(alicloudResult));
    }

    MockHttpServletResponse getCredentialCapabilities(Csp csp, CredentialType type)
            throws Exception {
        return mockMvc.perform(get("/xpanse/credentials/capabilities")
                        .param("cspName", csp.toValue())
                        .param("type", type.toValue())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    List<AbstractCredentialInfo> getCredentialCapabilities(Csp csp) {
        List<AbstractCredentialInfo> result = new ArrayList<>();
        if (csp == Csp.HUAWEI) {
            result = huaweiCloudPlugin.getCredentialDefinitions();
        } else if (csp == Csp.FLEXIBLE_ENGINE) {
            result = flexibleEnginePlugin.getCredentialDefinitions();
        } else if (csp == Csp.OPENSTACK) {
            result = openstackPlugin.getCredentialDefinitions();
        } else if (csp == Csp.SCS) {
            result = scsPlugin.getCredentialDefinitions();
        }
        credentialsCenter.getCredentialCapabilitiesValue(result);
        return result;
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetCredentialOpenApi() throws Exception {
        // Setup
        CredentialType variablesType = CredentialType.VARIABLES;
        // Setup
        Csp huawei = Csp.HUAWEI;
        Link huaweiResult = Link.of(getApiUrl(huawei, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse huaweiResponse = getCredentialOpenApi(huawei,
                variablesType);
        // Verify the results
        assertThat(huaweiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiResult));
        testGetCredentialCapabilitiesThrowsException(huawei);


        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        Link flexibleEngineResult = Link.of(getApiUrl(flexibleEngine, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse flexibleEngineResponse =
                getCredentialOpenApi(flexibleEngine,
                        variablesType);
        // Verify the results
        assertThat(flexibleEngineResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(flexibleEngineResult));

        testGetCredentialCapabilitiesThrowsException(flexibleEngine);

        // Setup
        Csp openstack = Csp.OPENSTACK;
        Link openstackResult = Link.of(getApiUrl(openstack, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse openstackResponse = getCredentialOpenApi(openstack,
                variablesType);
        // Verify the results
        assertThat(openstackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(openstackResult));

        testGetCredentialCapabilitiesThrowsException(openstack);

        // Setup
        Csp scs = Csp.SCS;
        Link scsResult = Link.of(getApiUrl(scs, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse scsResponse = getCredentialOpenApi(scs,
                variablesType);
        // Verify the results
        assertThat(scsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(scsResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(scsResult));
        testGetCredentialCapabilitiesThrowsException(scs);

        // Setup
        Csp alicloud = Csp.ALICLOUD;
        Response alicloudResult = Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(
                        String.format("Can't find suitable plugin for the Csp %s", alicloud)));
        // Run the test
        final MockHttpServletResponse alicloudResponse =
                getCredentialOpenApi(alicloud, variablesType);
        // Verify the results
        assertThat(alicloudResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(alicloudResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(alicloudResult));
    }

    MockHttpServletResponse getCredentialOpenApi(Csp csp, CredentialType type)
            throws Exception {
        return mockMvc.perform(get("/xpanse/credentials/openapi/{csp}/{type}", csp, type)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    void testGetCredentialCapabilitiesThrowsException(Csp csp) throws Exception {
        CredentialType httpType = CredentialType.HTTP_AUTHENTICATION;
        Response huaweiResult1 = Response.errorResponse(
                ResultType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE,
                Collections.singletonList(String.format("Not found credential definition with type"
                                + " %s of the cloud service provider %s", httpType.toValue(),
                        csp.toValue())));
        // Run the test
        final MockHttpServletResponse errorResponse = getCredentialOpenApi(csp, httpType);
        // Verify the results
        assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(huaweiResult1));

    }

    String getApiUrl(Csp csp, CredentialType type) {
        return String.format("http://localhost/openapi/%s_%s_credentialApi.html",
                csp.toValue(), type.toValue());
    }


}
