/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.security.model.TokenResponse;
import org.eclipse.xpanse.modules.security.zitadel.ZitadelAuthorizationService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface methods for Zitadel oauth2 authentication.
 */
@Slf4j
@RestController
@Profile("zitadel")
@CrossOrigin
public class ZitadelAuthorizationApi {

    private final ZitadelAuthorizationService zitadelAuthorizationService;

    public ZitadelAuthorizationApi(ZitadelAuthorizationService zitadelAuthorizationService) {
        this.zitadelAuthorizationService = zitadelAuthorizationService;
    }


    @Tag(name = "Auth Management",
            description = "APIs for user authentication and authorization.")
    @Operation(description = "Get and redirect authorization url for user to authenticate.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auth/authorize")
    void authorize(HttpServletResponse response) throws IOException {
        String authorizeUrl = zitadelAuthorizationService.getAuthorizeUrl();
        response.sendRedirect(authorizeUrl);
    }

    @Tag(name = "Auth Management",
            description = "APIs for user authentication and authorization.")
    @Operation(description = "Get token info by authorization code.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/auth/token")
    TokenResponse getAccessToken(
            @Parameter(name = "code", required = true, description = "The authorization code.")
                    String code,
            @Parameter(name = "state", description = "Opaque value used to maintain state.")
                    String state) {
        return zitadelAuthorizationService.getAccessToken(code);
    }
}
