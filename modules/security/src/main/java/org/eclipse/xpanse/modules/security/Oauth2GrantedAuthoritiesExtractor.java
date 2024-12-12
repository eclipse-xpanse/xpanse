/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${authorization.default.role}")
    private String defaultRole;

    @Value("${authorization.userid.key}")
    private String userIdKey;

    @Value("${authorization.granted.roles.scope}")
    private String grantedRolesScope;

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Map<String, Object> claims) {
        Set<GrantedAuthority> roles;
        String userId = (String) claims.get(userIdKey);
        Map<String, Object> rolesClaim = (Map<String, Object>) claims.get(grantedRolesScope);
        if (Objects.isNull(rolesClaim) || rolesClaim.isEmpty()) {
            roles = Set.of(new SimpleGrantedAuthority(defaultRole));
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
