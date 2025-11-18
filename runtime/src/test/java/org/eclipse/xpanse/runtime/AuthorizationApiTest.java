package org.eclipse.xpanse.runtime;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.logging.LoggingKeyConstant;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-local,test,dev"})
@AutoConfigureMockMvc
class AuthorizationApiTest extends ApisTestCommon {

    @RegisterExtension
    static WireMockExtension wireMockExtension =
            WireMockExtension.newInstance()
                    .options(
                            wireMockConfig()
                                    .dynamicPort()
                                    .extensions(
                                            new ResponseTemplateTransformer(
                                                    TemplateEngine.defaultTemplateEngine(),
                                                    false,
                                                    new ClasspathFileSource(
                                                            "src/test/resources/mappings"),
                                                    Collections.emptyList())))
                    .build();

    @Value("${xpanse.security.oauth.auth-provider-endpoint}")
    private String iamServiceEndpoint;

    @Test
    void testAuthorize() throws Exception {

        // Setup
        String redirectUrl = iamServiceEndpoint + "/oauth/v2/authorize?";

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/auth/authorize").accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        assertTrue(StringUtils.isEmpty(response.getContentAsString()));
        assertTrue(StringUtils.isNotEmpty(response.getRedirectedUrl()));
        assertTrue(response.getRedirectedUrl().startsWith(redirectUrl));
    }

    @Test
    void testGetAccessToken() throws Exception {
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/auth/token")
                                        .accept(MediaType.APPLICATION_JSON)
                                        .param("code", "code"))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    void testCallApiUnauthorized() throws Exception {
        // SetUp
        ErrorResponse errorResponseModel =
                ErrorResponse.errorResponse(
                        ErrorType.UNAUTHORIZED,
                        Collections.singletonList(
                                "Full authentication is required to access this resource"));
        String resBody = objectMapper.writeValueAsString(errorResponseModel);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/health").accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());
        assertNotNull(response.getHeader(LoggingKeyConstant.HEADER_TRACKING_ID));
    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testCallApiAccessDenied() throws Exception {
        // SetUp
        ErrorResponse errorResponseModel =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED,
                        Collections.singletonList(ErrorType.ACCESS_DENIED.toValue()));
        String resBody = objectMapper.writeValueAsString(errorResponseModel);
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/services").accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());
        assertNotNull(response.getHeader(LoggingKeyConstant.HEADER_TRACKING_ID));
    }
}
