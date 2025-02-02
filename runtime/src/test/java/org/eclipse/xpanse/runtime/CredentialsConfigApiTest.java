package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.plugins.flexibleengine.FlexibleEngineOrchestratorPlugin;
import org.eclipse.xpanse.plugins.huaweicloud.HuaweiCloudOrchestratorPlugin;
import org.eclipse.xpanse.plugins.openstacktestlab.OpenstackTestlabOrchestratorPlugin;
import org.eclipse.xpanse.plugins.plusserver.PlusServerOrchestratorPlugin;
import org.eclipse.xpanse.plugins.regiocloud.RegioCloudOrchestratorPlugin;
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
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test, dev"})
@AutoConfigureMockMvc
class CredentialsConfigApiTest extends ApisTestCommon {

    @Resource private HuaweiCloudOrchestratorPlugin huaweiCloudPlugin;
    @Resource private FlexibleEngineOrchestratorPlugin flexibleEnginePlugin;
    @Resource private OpenstackTestlabOrchestratorPlugin openstackPlugin;
    @Resource private PlusServerOrchestratorPlugin plusServerPlugin;
    @Resource private RegioCloudOrchestratorPlugin regioCloudPlugin;
    @Resource private CredentialCenter credentialsCenter;
    @Resource private PluginManager pluginManager;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetCredentialTypes() throws Exception {
        // Setup
        Csp huawei = Csp.HUAWEI_CLOUD;
        List<CredentialType> huaweiCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse huaweiResponse = getCredentialTypes(huawei);
        // Verify the results
        assertThat(huaweiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiCredentialTypes));

        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        List<CredentialType> flexibleEngineCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse flexibleEngineResponse = getCredentialTypes(flexibleEngine);
        // Verify the results
        assertThat(flexibleEngineResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineCredentialTypes));

        // Setup
        Csp openstack = Csp.OPENSTACK_TESTLAB;
        List<CredentialType> openstackCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse openstackResponse = getCredentialTypes(openstack);
        // Verify the results
        assertThat(openstackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(openstackCredentialTypes));

        // Setup
        Csp plusServer = Csp.PLUS_SERVER;
        List<CredentialType> plusServerCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse plusServerResponse = getCredentialTypes(plusServer);
        // Verify the results
        assertThat(plusServerResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(plusServerResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(plusServerCredentialTypes));

        // Setup
        Csp regioCloud = Csp.REGIO_CLOUD;
        List<CredentialType> regioCloudCredentialTypes =
                Collections.singletonList(CredentialType.VARIABLES);
        // Run the test
        final MockHttpServletResponse regioCloudResponse = getCredentialTypes(regioCloud);
        // Verify the results
        assertThat(regioCloudResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(regioCloudResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(regioCloudCredentialTypes));

        // Setup
        Csp aws = Csp.AWS;
        ErrorResponse awsResult =
                ErrorResponse.errorResponse(
                        ErrorType.PLUGIN_NOT_FOUND,
                        Collections.singletonList(
                                String.format(
                                        "Can't find suitable plugin for the Csp %s",
                                        aws.toValue())));
        // Run the test
        final MockHttpServletResponse awsResponse = getCredentialTypes(aws);
        // Verify the results
        assertThat(awsResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(awsResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(awsResult));
    }

    MockHttpServletResponse getCredentialTypes(Csp csp) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/credential_types")
                                .param("cspName", csp.toValue())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetCredentialCapabilities() throws Exception {
        CredentialType variablesType = CredentialType.VARIABLES;
        CredentialType httpType = CredentialType.HTTP_AUTHENTICATION;
        // Setup
        Csp huawei = Csp.HUAWEI_CLOUD;
        List<AbstractCredentialInfo> huaweiResult = getCredentialCapabilities(huawei);
        // Run the test
        final MockHttpServletResponse huaweiResponse =
                getCredentialCapabilities(huawei, variablesType);
        // Verify the results
        assertThat(huaweiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiResult));

        // Run the test
        final MockHttpServletResponse huaweiResponse1 = getCredentialCapabilities(huawei, httpType);
        // Verify the results
        assertThat(huaweiResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        List<AbstractCredentialInfo> flexibleEngineResult =
                getCredentialCapabilities(flexibleEngine);
        // Run the test
        final MockHttpServletResponse flexibleEngineResponse =
                getCredentialCapabilities(flexibleEngine, variablesType);
        // Verify the results
        assertThat(flexibleEngineResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineResult));

        // Run the test
        final MockHttpServletResponse flexibleEngineResponse1 =
                getCredentialCapabilities(flexibleEngine, httpType);
        // Verify the results
        assertThat(flexibleEngineResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp openstack = Csp.OPENSTACK_TESTLAB;
        List<AbstractCredentialInfo> openstackResult = getCredentialCapabilities(openstack);
        // Run the test
        final MockHttpServletResponse openstackResponse =
                getCredentialCapabilities(openstack, variablesType);
        // Verify the results
        assertThat(openstackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(openstackResult));

        // Run the test
        final MockHttpServletResponse openstackResponse1 =
                getCredentialCapabilities(openstack, httpType);
        // Verify the results
        assertThat(openstackResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp plusServer = Csp.PLUS_SERVER;
        List<AbstractCredentialInfo> plusServerResult = getCredentialCapabilities(plusServer);
        // Run the test
        final MockHttpServletResponse plusServerResponse =
                getCredentialCapabilities(plusServer, variablesType);
        // Verify the results
        assertThat(plusServerResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(plusServerResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(plusServerResult));
        // Run the test
        final MockHttpServletResponse plusServerResponse1 =
                getCredentialCapabilities(plusServer, httpType);
        // Verify the results
        assertThat(plusServerResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(plusServerResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp regioCloud = Csp.REGIO_CLOUD;
        List<AbstractCredentialInfo> regioCloudResult = getCredentialCapabilities(regioCloud);
        // Run the test
        final MockHttpServletResponse regioCloudResponse =
                getCredentialCapabilities(regioCloud, variablesType);
        // Verify the results
        assertThat(regioCloudResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(regioCloudResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(regioCloudResult));
        // Run the test
        final MockHttpServletResponse regioCloudResponse1 =
                getCredentialCapabilities(regioCloud, httpType);
        // Verify the results
        assertThat(regioCloudResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(regioCloudResponse1.getContentAsString()).isEqualTo("[]");

        // Setup
        Csp aliCloud = Csp.ALIBABA_CLOUD;
        ErrorResponse aliCloudResult =
                ErrorResponse.errorResponse(
                        ErrorType.PLUGIN_NOT_FOUND,
                        Collections.singletonList(
                                String.format(
                                        "Can't find suitable plugin for the Csp %s",
                                        aliCloud.toValue())));
        // Run the test
        final MockHttpServletResponse aliCloudResponse =
                getCredentialCapabilities(aliCloud, variablesType);
        // Verify the results
        assertThat(aliCloudResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(aliCloudResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(aliCloudResult));
    }

    MockHttpServletResponse getCredentialCapabilities(Csp csp, CredentialType type)
            throws Exception {
        return mockMvc.perform(
                        get("/xpanse/credentials/capabilities")
                                .param("cspName", csp.toValue())
                                .param("type", type.toValue())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    List<AbstractCredentialInfo> getCredentialCapabilities(Csp csp) {
        List<AbstractCredentialInfo> result = new ArrayList<>();
        if (csp == Csp.HUAWEI_CLOUD) {
            result = huaweiCloudPlugin.getCredentialDefinitions();
        } else if (csp == Csp.FLEXIBLE_ENGINE) {
            result = flexibleEnginePlugin.getCredentialDefinitions();
        } else if (csp == Csp.OPENSTACK_TESTLAB) {
            result = openstackPlugin.getCredentialDefinitions();
        } else if (csp == Csp.PLUS_SERVER) {
            result = plusServerPlugin.getCredentialDefinitions();
        } else if (csp == Csp.REGIO_CLOUD) {
            result = regioCloudPlugin.getCredentialDefinitions();
        }
        credentialsCenter.getCredentialCapabilitiesValue(result);
        return result;
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetSitesOfCsp() throws Exception {
        // SetUp
        Csp csp = Csp.HUAWEI_CLOUD;
        List<String> sites1 = pluginManager.getOrchestratorPlugin(csp).getSites();

        // Run the test
        final MockHttpServletResponse response1 =
                mockMvc.perform(
                                get("/xpanse/csps/{cspName}/sites", csp.toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(response1.getStatus(), HttpStatus.OK.value());
        assertTrue(StringUtils.isNotEmpty(response1.getContentAsString()));
        assertEquals(objectMapper.writeValueAsString(sites1), response1.getContentAsString());
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetCredentialOpenApi() throws Exception {
        // Setup
        CredentialType variablesType = CredentialType.VARIABLES;
        // Setup
        Csp huawei = Csp.HUAWEI_CLOUD;
        Link huaweiResult = Link.of(getApiUrl(huawei, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse huaweiResponse = getCredentialOpenApi(huawei, variablesType);
        // Verify the results
        assertThat(huaweiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(huaweiResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiResult));
        testGetCredentialCapabilitiesThrowsException(huawei);

        // Setup
        Csp flexibleEngine = Csp.FLEXIBLE_ENGINE;
        Link flexibleEngineResult = Link.of(getApiUrl(flexibleEngine, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse flexibleEngineResponse =
                getCredentialOpenApi(flexibleEngine, variablesType);
        // Verify the results
        assertThat(flexibleEngineResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(flexibleEngineResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(flexibleEngineResult));

        testGetCredentialCapabilitiesThrowsException(flexibleEngine);

        // Setup
        Csp openstack = Csp.OPENSTACK_TESTLAB;
        Link openstackResult = Link.of(getApiUrl(openstack, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse openstackResponse =
                getCredentialOpenApi(openstack, variablesType);
        // Verify the results
        assertThat(openstackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(openstackResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(openstackResult));

        testGetCredentialCapabilitiesThrowsException(openstack);

        // Setup
        Csp plusServer = Csp.PLUS_SERVER;
        Link plusServerResult = Link.of(getApiUrl(plusServer, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse plusServerResponse =
                getCredentialOpenApi(plusServer, variablesType);
        // Verify the results
        assertThat(plusServerResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(plusServerResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(plusServerResult));
        testGetCredentialCapabilitiesThrowsException(plusServer);

        // Setup
        Csp regioCloud = Csp.REGIO_CLOUD;
        Link regioCloudResult = Link.of(getApiUrl(regioCloud, variablesType), "OpenApi");
        // Run the test
        final MockHttpServletResponse regioCloudResponse =
                getCredentialOpenApi(regioCloud, variablesType);
        // Verify the results
        assertThat(regioCloudResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(regioCloudResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(regioCloudResult));
        testGetCredentialCapabilitiesThrowsException(regioCloud);

        // Setup
        Csp aliCloud = Csp.ALIBABA_CLOUD;
        ErrorResponse aliCloudResult =
                ErrorResponse.errorResponse(
                        ErrorType.PLUGIN_NOT_FOUND,
                        Collections.singletonList(
                                String.format(
                                        "Can't find suitable plugin for the Csp %s",
                                        aliCloud.toValue())));
        // Run the test
        final MockHttpServletResponse aliCloudResponse =
                getCredentialOpenApi(aliCloud, variablesType);
        // Verify the results
        assertThat(aliCloudResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(aliCloudResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(aliCloudResult));
    }

    MockHttpServletResponse getCredentialOpenApi(Csp csp, CredentialType type) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/credentials/openapi/{csp}/{type}", csp, type)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    void testGetCredentialCapabilitiesThrowsException(Csp csp) throws Exception {
        CredentialType httpType = CredentialType.HTTP_AUTHENTICATION;
        ErrorResponse huaweiResult1 =
                ErrorResponse.errorResponse(
                        ErrorType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE,
                        Collections.singletonList(
                                String.format(
                                        "Not found credential definition with type"
                                                + " %s of the cloud service provider %s",
                                        httpType.toValue(), csp.toValue())));
        // Run the test
        final MockHttpServletResponse errorResponse = getCredentialOpenApi(csp, httpType);
        // Verify the results
        assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(huaweiResult1));
    }

    String getApiUrl(Csp csp, CredentialType type) {
        return String.format(
                "http://localhost/openapi/%s_%s_credentialApi.html", csp.toValue(), type.toValue());
    }
}
