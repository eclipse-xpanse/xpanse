/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.introspector;

import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.DEFAULT_ROLE;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.GRANTED_ROLES_KEY;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.USERID_KEY;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.USERNAME_KEY;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.CollectionUtils;

/**
 * Customize the ZitadelAuthoritiesOpaqueTokenIntrospector implements OpaqueTokenIntrospector.
 */
@Slf4j
public class ZitadelOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private final OpaqueTokenIntrospector delegate;

    /**
     * Constructor.
     *
     * @param introspectionUri The url of IAM server to verify token and get user.
     * @param clientId         The id of api client created in IAM server.
     * @param clientSecret     The secret of api client created in IAM server.
     */
    public ZitadelOpaqueTokenIntrospector(String introspectionUri,
                                          String clientId,
                                          String clientSecret) {
        delegate = new NimbusOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    }

    /**
     * Verify token and put current user info into OAuth2AuthenticatedPrincipal.
     *
     * @param token The token of current user.
     * @return OAuth2AuthenticatedPrincipal
     */
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2AuthenticatedPrincipal principal = this.delegate.introspect(token);
        return new DefaultOAuth2AuthenticatedPrincipal(
                principal.getName(), principal.getAttributes(), extractAuthorities(principal));
    }

    /**
     * Get granted authorities of current user.
     *
     * @param principal The instance of OAuth2AuthenticatedPrincipal.
     * @return granted authorities.
     */
    private Collection<GrantedAuthority> extractAuthorities(
            OAuth2AuthenticatedPrincipal principal) {

        Collection<GrantedAuthority> roleSet;
        JSONObject roleObject = principal.getAttribute(GRANTED_ROLES_KEY);
        String userName = principal.getAttribute(USERNAME_KEY);
        String userId = principal.getAttribute(USERID_KEY);

        if (Objects.isNull(roleObject)) {
            roleSet = Set.of(new SimpleGrantedAuthority(DEFAULT_ROLE));
            log.info("Get user [id:{},userName:{}] granted authorities is null,"
                    + " set default authority user", userId, userName);
        } else {
            Set<String> roles = roleObject.keySet();
            if (CollectionUtils.isEmpty(roles)) {
                roleSet = Set.of(new SimpleGrantedAuthority(DEFAULT_ROLE));
                log.info("Get user [id:{},userName:{}] granted authorities is empty,"
                        + " set default authority user", userId, userName);
            } else {
                roleSet = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
                log.info("Get user [id:{},userName:{}] granted authorities:{}.",
                        userId, userName, roleSet);
            }
        }
        return roleSet;
    }
}