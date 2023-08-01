/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.common;

import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

/**
 * Convert authentication to get current user info.
 */
public class ZitadelUserAuthenticationConverter {

    /**
     * Get Authentication from SecurityContext and convert to CurrentUerInfo.
     *
     * @return Object of CurrentUserInfo.
     */
    public static CurrentUserInfo getCurrentUserInfo() {
        BearerTokenAuthentication tokenAuthentication =
                (BearerTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(tokenAuthentication)) {
            return null;
        } else {
            CurrentUserInfo currentUserInfo = new CurrentUserInfo();
            currentUserInfo.setUserId(
                    tokenAuthentication.getTokenAttributes().get("sub").toString());
            if (tokenAuthentication.getTokenAttributes().containsKey("preferred_username")) {
                currentUserInfo.setUserName(
                        tokenAuthentication.getTokenAttributes().get("preferred_username")
                                .toString());
            }
            List<String> roles = tokenAuthentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList();
            currentUserInfo.setRoles(roles);
            return currentUserInfo;
        }
    }

}