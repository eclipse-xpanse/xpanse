/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Converter to map claims to GrantedAuthorities. This class is used by both JWT and OpaqueToken.
 */
@Slf4j
@Profile("oauth")
@Component
public class Oauth2GrantedAuthoritiesExtractor
        implements Converter<Map<String, Object>, Collection<GrantedAuthority>> {

    private final SecurityProperties securityProperties;

    @Autowired
    public Oauth2GrantedAuthoritiesExtractor(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Map<String, Object> claims) {
        Set<GrantedAuthority> roles;
        String userId =
                (String) claims.get(securityProperties.getOauth().getClaims().getUserIdKey());
        Map<String, Object> rolesClaim =
                (Map<String, Object>)
                        claims.get(securityProperties.getOauth().getClaims().getGrantedRolesKey());
        if (Objects.isNull(rolesClaim) || rolesClaim.isEmpty()) {
            roles =
                    Set.of(
                            new SimpleGrantedAuthority(
                                    securityProperties.getOauth().getDefaultRole()));
            log.info(
                    "Get user [id:{}] granted authorities is empty,"
                            + " set default authority user",
                    userId);
        } else {
            roles =
                    rolesClaim.keySet().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet());
            log.info("Get user [id:{}] granted authorities:{}.", userId, roles);
        }
        return roles;
    }
}
