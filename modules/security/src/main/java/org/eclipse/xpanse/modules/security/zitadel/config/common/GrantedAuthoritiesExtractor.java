/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config.common;

import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.DEFAULT_ROLE;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.GRANTED_ROLES_KEY;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.USERID_KEY;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Converter to map claims to GrantedAuthorities. This class is used by both JWT and OpaqueToken.
 */
@Slf4j
@Component
public class GrantedAuthoritiesExtractor implements
        Converter<Map<String, Object>, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Map<String, Object> claims) {
        Set<GrantedAuthority> roles;
        String userId = (String) claims.get(USERID_KEY);
        Map<String, Object> rolesClaim = (Map<String, Object>) claims.get(GRANTED_ROLES_KEY);
        if (Objects.isNull(rolesClaim) || rolesClaim.isEmpty()) {
            roles = Set.of(new SimpleGrantedAuthority(DEFAULT_ROLE));
            log.info("Get user [id:{}] granted authorities is empty,"
                    + " set default authority user", userId);
        } else {
            roles = rolesClaim
                    .keySet()
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
            log.info("Get user [id:{}] granted authorities:{}.", userId, roles);
        }
        return roles;
    }
}