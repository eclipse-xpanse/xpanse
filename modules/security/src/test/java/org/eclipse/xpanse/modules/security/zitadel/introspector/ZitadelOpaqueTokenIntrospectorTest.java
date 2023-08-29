package org.eclipse.xpanse.modules.security.zitadel.introspector;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ZitadelOpaqueTokenIntrospector.class, String.class})
class ZitadelOpaqueTokenIntrospectorTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .extensions(new ResponseTemplateTransformer(true)))
            .build();
    private ZitadelOpaqueTokenIntrospector testOpaqueTokenIntrospector;

    @BeforeEach
    void setUp() {
        String introspectionUri = wireMockExtension.getRuntimeInfo().getHttpBaseUrl()
                + "/oauth/v2/introspect";
        testOpaqueTokenIntrospector =
                new ZitadelOpaqueTokenIntrospector(introspectionUri,
                        "clientId", "clientSecret");
    }

    @Test
    void testIntrospect() {
        // Setup
        SimpleGrantedAuthority role = new SimpleGrantedAuthority("admin");
        String userId = "221647436064489729";
        String userName = "xpanse-admin";
        // Run the test
        final OAuth2AuthenticatedPrincipal result =
                testOpaqueTokenIntrospector.introspect("token");
        // Verify the results
        assertTrue(result.getAuthorities().contains(role));
        assertEquals(userId, result.getAttribute("sub"));
        assertEquals(userName, result.getAttribute("username"));
    }
}
