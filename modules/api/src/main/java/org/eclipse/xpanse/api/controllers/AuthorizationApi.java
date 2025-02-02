/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.models.security.TokenResponse;
import org.eclipse.xpanse.modules.security.auth.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.auth.IdentityProviderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST interface methods for authentication. */
@Slf4j
@Profile("oauth")
@CrossOrigin
@RestController
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class AuthorizationApi {

    @Resource private IdentityProviderManager identityProviderManager;

    @Tag(name = "AuthManagement", description = "APIs for user authentication and authorization.")
    @Operation(description = "Get and redirect authorization url for user to authenticate.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auth/authorize")
    @AuditApiRequest(enabled = false)
    void authorize(HttpServletResponse response) throws IOException {
        IdentityProviderService identityProviderService =
                identityProviderManager.getActiveIdentityProviderService();
        String authorizeUrl = "";
        if (Objects.nonNull(identityProviderService)) {
            authorizeUrl = identityProviderService.getAuthorizeUrl();
        }
        if (StringUtils.isNotEmpty(authorizeUrl)) {
            response.sendRedirect(authorizeUrl);
        } else {
            PrintWriter writer = response.getWriter();
            writer.write("No active identity provider found.");
        }
    }

    @Tag(name = "AuthManagement", description = "APIs for user authentication and authorization.")
    @Operation(description = "Get token info by authorization code.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/auth/token")
    @AuditApiRequest(enabled = false)
    TokenResponse getAccessToken(
            @Parameter(name = "code", required = true, description = "The authorization code.")
                    String code,
            @Parameter(name = "state", description = "Opaque value used to maintain state.")
                    String state) {
        IdentityProviderService identityProviderService =
                identityProviderManager.getActiveIdentityProviderService();
        if (Objects.nonNull(identityProviderService)) {
            return identityProviderService.getAccessToken(code);
        }
        return null;
    }
}
