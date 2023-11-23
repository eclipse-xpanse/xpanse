package org.eclipse.xpanse.runtime;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockBearerTokenAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import jakarta.annotation.Resource;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.security.model.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class AuthorizationApiTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .extensions(
                            new ResponseTemplateTransformer(TemplateEngine.defaultTemplateEngine(),
                                    false, new ClasspathFileSource("src/test/resources/mappings"),
                                    Collections.emptyList())))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${authorization.server.endpoint}")
    private String iamServiceEndpoint;

    @Qualifier("zitadelRestTemplate")
    @Resource
    private MockMvc mockMvc;


    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testAuthorize() throws Exception {

        // Setup
        String redirectUrl = iamServiceEndpoint + "/oauth/v2/authorize?";

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/auth/authorize")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        assertTrue(StringUtils.isEmpty(response.getContentAsString()));
        assertTrue(StringUtils.isNotEmpty(response.getRedirectedUrl()));
        assertTrue(response.getRedirectedUrl().startsWith(redirectUrl));
    }


    @Test
    void testGetAccessToken() throws Exception {

        // Setup
        when(restTemplate.postForEntity(anyString(), any(),
                eq(TokenResponse.class))).thenReturn(getAccessToken(
                wireMockExtension.getRuntimeInfo().getHttpBaseUrl() + "/oauth/v2/token"));

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/auth/token")
                        .accept(MediaType.APPLICATION_JSON).param("code", "code"))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
    }


    @Test
    void testGetAccessTokenThrowsIllegalArgumentException() {

        // Run the test
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> mockMvc.perform(get("/auth/token")
                                .accept(MediaType.APPLICATION_JSON).param("code", null))
                        .andReturn().getResponse());
    }

    @Test
    void testCallApiUnauthorized() throws Exception {
        // SetUp
        Response responseModel = Response.errorResponse(ResultType.UNAUTHORIZED,
                Collections.singletonList(ResultType.UNAUTHORIZED.toValue()));
        String resBody = objectMapper.writeValueAsString(responseModel);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());
    }

    @Test
    @WithMockBearerTokenAuthentication(authorities = {"csp"},
            attributes = @OpenIdClaims(sub = "csp-id", preferredUsername = "xpanse-csp"))
    void testCallApiAccessDenied() throws Exception {
        // SetUp
        Response responseModel = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList(ResultType.ACCESS_DENIED.toValue()));
        String resBody = objectMapper.writeValueAsString(responseModel);
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());
    }

    @Test
    @WithMockJwtAuth(authorities = {"user"},
            claims = @OpenIdClaims(sub = "user-id", preferredUsername = "xpanse-user"))
    void testCallApiWell() throws Exception {
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals("[]", response.getContentAsString());
    }


    private ResponseEntity<TokenResponse> getAccessToken(String url) {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        return new ResponseEntity<>(testRestTemplate.postForObject(url, "{}", TokenResponse.class),
                HttpStatus.OK);
    }

}
