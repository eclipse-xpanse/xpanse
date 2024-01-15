/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.common;

import java.io.Serial;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Data class to hold information all information of authenticated user.
 * this will be available in the security context.
 */
public class XpanseAuthentication extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = -2797266050799781972L;
    @Getter
    private final Map<String, Object> claims;
    private final Principal principal;
    @Getter
    private final String token;

    /**
     * Constructor to create Xpanse Authentication objects.
     *
     * @param username userId of the authenticated user
     * @param authorities roles
     * @param claims all claims received in the token
     * @param token actual token.
     */
    public XpanseAuthentication(String username, Collection<? extends GrantedAuthority> authorities,
                                Map<String, Object> claims, String token) {
        super(authorities);
        this.claims = Collections.unmodifiableMap(claims);
        this.principal = new SimplePrincipal(username);
        this.token = token;
        super.setAuthenticated(true);
    }

    @Override
    public String getCredentials() {
        return this.token;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }
}
