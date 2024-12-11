package org.eclipse.xpanse.modules.security.zitadel.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.security.Oauth2GrantedAuthoritiesExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

public class GrantedAuthoritiesExtractorTest {

    Oauth2GrantedAuthoritiesExtractor grantedAuthoritiesExtractor =
            new Oauth2GrantedAuthoritiesExtractor();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(grantedAuthoritiesExtractor, "defaultRole", "user");
        ReflectionTestUtils.setField(grantedAuthoritiesExtractor, "userIdKey", "sub");
        ReflectionTestUtils.setField(
                grantedAuthoritiesExtractor,
                "grantedRolesScope",
                "urn:zitadel:iam:org:project:roles");
    }

    @Test
    void testConvertJwt() {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> rolesClaims = new HashMap<>();
        rolesClaims.put("admin", "");
        claims.put("urn:zitadel:iam:org:project:roles", rolesClaims);
        claims.put("sub", "user-id");

        Collection<GrantedAuthority> grantedAuthorities =
                grantedAuthoritiesExtractor.convert(claims);
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("admin");

        assertNotNull(grantedAuthorities);
        assertTrue(grantedAuthorities.contains(grantedAuthority));
    }

    @Test
    void testConvertJwtWithEmptyRoles() {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> rolesClaims = new HashMap<>();
        claims.put("sub", "user-id");
        claims.put("urn:zitadel:iam:org:project:roles", rolesClaims);

        Collection<GrantedAuthority> grantedAuthorities =
                grantedAuthoritiesExtractor.convert(claims);
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("user");

        assertNotNull(grantedAuthorities);
        assertTrue(grantedAuthorities.contains(grantedAuthority));
    }

    @Test
    void testConvertJwtWithNullRoleClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-id");
        Collection<GrantedAuthority> grantedAuthorities =
                grantedAuthoritiesExtractor.convert(claims);
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("user");
        assertNotNull(grantedAuthorities);
        assertTrue(grantedAuthorities.contains(grantedAuthority));
    }
}
