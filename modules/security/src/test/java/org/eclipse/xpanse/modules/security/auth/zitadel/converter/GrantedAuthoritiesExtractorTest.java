package org.eclipse.xpanse.modules.security.auth.zitadel.converter;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.security.auth.Oauth2GrantedAuthoritiesExtractor;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class GrantedAuthoritiesExtractorTest {

    Oauth2GrantedAuthoritiesExtractor grantedAuthoritiesExtractor =
            new Oauth2GrantedAuthoritiesExtractor(
                    Instancio.of(SecurityProperties.class)
                            .set(field(SecurityProperties.OAuth::getDefaultRole), "user")
                            .set(field(SecurityProperties.Claims::getUserIdKey), "sub")
                            .set(
                                    field(SecurityProperties.Claims::getGrantedRolesKey),
                                    "urn:zitadel:iam:org:project:roles")
                            .create());

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
