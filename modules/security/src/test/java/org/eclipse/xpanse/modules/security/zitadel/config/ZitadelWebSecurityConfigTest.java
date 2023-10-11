package org.eclipse.xpanse.modules.security.zitadel.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ZitadelWebSecurityConfig.class, String.class})
class ZitadelWebSecurityConfigTest {

    @Value("${authorization.token.type:JWT}")
    private String authTokenType;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://iam.xpanse.site}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri:https://iam.xpanse.site/oauth/v2/introspect}")
    private String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id:221665786966638595@eclipse-xpanse}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret:2S7yZR4jWauyE3wLHM1h5asc0WNWdBUXAw2Ill3Pmu0qM4D3LWBCwikXbsG81ycl}")
    private String clientSecret;

    private ZitadelWebSecurityConfig zitadelWebSecurityConfigUnderTest;

    @BeforeEach
    void setUp() {
        zitadelWebSecurityConfigUnderTest = new ZitadelWebSecurityConfig();
        ReflectionTestUtils.setField(zitadelWebSecurityConfigUnderTest, "authTokenType",
                authTokenType);
        ReflectionTestUtils.setField(zitadelWebSecurityConfigUnderTest, "issuerUri", issuerUri);
        ReflectionTestUtils.setField(zitadelWebSecurityConfigUnderTest, "introspectionUri",
                introspectionUri);
        ReflectionTestUtils.setField(zitadelWebSecurityConfigUnderTest, "clientId", clientId);
        ReflectionTestUtils.setField(zitadelWebSecurityConfigUnderTest, "clientSecret",
                clientSecret);
    }

    @Test
    void testCorsConfigurationSource() {
        // Setup
        // Run the test
        final CorsConfigurationSource result =
                zitadelWebSecurityConfigUnderTest.corsConfigurationSource();

        // Verify the results
        Assertions.assertNotNull(result);
    }

    @Test
    void testJwtDecoder() {
        // Setup
        // Run the test
        final JwtDecoder result = zitadelWebSecurityConfigUnderTest.jwtDecoder();

        // Verify the results
        Assertions.assertNotNull(result);
    }

    @Test
    void testGrantedAuthoritiesExtractor() {
        // Setup
        // Run the test
        final Converter<Jwt, ? extends AbstractAuthenticationToken> result =
                zitadelWebSecurityConfigUnderTest.grantedAuthoritiesExtractor();

        // Verify the results
        Assertions.assertNotNull(result);
    }

    @Test
    void testConvertJwt() {
        ZitadelWebSecurityConfig.GrantedAuthoritiesExtractor grantedAuthoritiesExtractor
                = new ZitadelWebSecurityConfig.GrantedAuthoritiesExtractor();

        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer Token");

        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> rolesClaims = new HashMap<>();
        rolesClaims.put("admin", "");
        claims.put("urn:zitadel:iam:org:project:roles", rolesClaims);
        claims.put("sub", "user-id");

        Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.MAX, headers, claims);
        Collection<GrantedAuthority> grantedAuthorities = grantedAuthoritiesExtractor.convert(jwt);

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("admin");

        assertNotNull(grantedAuthorities);
        assertTrue(grantedAuthorities.contains(grantedAuthority));
    }

    @Test
    void testConvertJwtWithEmptyRoles() {
        ZitadelWebSecurityConfig.GrantedAuthoritiesExtractor grantedAuthoritiesExtractor
                = new ZitadelWebSecurityConfig.GrantedAuthoritiesExtractor();

        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer Token");

        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> rolesClaims = new HashMap<>();
        claims.put("sub", "user-id");
        claims.put("urn:zitadel:iam:org:project:roles", rolesClaims);

        Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.MAX, headers, claims);
        Collection<GrantedAuthority> grantedAuthorities = grantedAuthoritiesExtractor.convert(jwt);

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("user");

        assertNotNull(grantedAuthorities);
        assertTrue(grantedAuthorities.contains(grantedAuthority));
    }

    @Test
    void testConvertJwtWithNullRoleClaims() {
        ZitadelWebSecurityConfig.GrantedAuthoritiesExtractor grantedAuthoritiesExtractor
                = new ZitadelWebSecurityConfig.GrantedAuthoritiesExtractor();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer Token");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-id");

        Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.MAX, headers, claims);
        Collection<GrantedAuthority> grantedAuthorities = grantedAuthoritiesExtractor.convert(jwt);

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("user");
        assertNotNull(grantedAuthorities);
        assertTrue(grantedAuthorities.contains(grantedAuthority));
    }
}
