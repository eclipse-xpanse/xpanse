package org.eclipse.xpanse.runtime;

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;

/**
 * Class to configure the JWT token mock.
 */
public abstract class AbstractJwtTestConfiguration {

    public void updateJwtInSecurityContext(Map<String, Object> claims, List<String> roles) {
        Map<String, Object> updatedClaims =  claims.isEmpty() ? Collections.singletonMap("sub", "userId") : claims;
        final JwtAuthenticationToken
                auth =
                (JwtAuthenticationToken) TestSecurityContextHolder.getContext().getAuthentication();
        final Jwt
                jwt = new Jwt("test", Instant.now(), Instant.now(),
                Collections.singletonMap("alg", "none"),
                updatedClaims);
        when(auth.getPrincipal()).thenReturn(jwt);
        when(auth.getCredentials()).thenReturn(jwt);
        when(auth.getToken()).thenReturn(jwt);
        when(auth.getTokenAttributes()).thenReturn(updatedClaims);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        when(auth.getAuthorities()).thenReturn(authorities);
    }

    public void updateJwtInSecurityContextWithSpecificUser(Map<String, Object> claims, List<String> roles, String userId) {
        Map<String, Object> updatedClaims = new HashMap<>();
        if (claims.isEmpty()) {
            updatedClaims.put("sub", userId);
        } else {
            claims.put("sub", userId);
            updatedClaims = claims;

        }
        updateJwtInSecurityContext(updatedClaims, roles);
    }
}
