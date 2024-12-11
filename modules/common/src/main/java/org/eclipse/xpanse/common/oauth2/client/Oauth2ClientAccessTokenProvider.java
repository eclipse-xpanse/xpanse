/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.common.oauth2.client;

import static org.eclipse.xpanse.common.oauth2.client.Oauth2ClientConfiguration.OAUTH_CLIENT_ID;

import jakarta.annotation.Resource;
import java.util.Objects;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

/**
 * Bean provides authorization token from the authenticated client. This method wraps all underlying
 * complexities and directly returns the token that can be used by any clients within the
 * application.
 */
@Component
@Profile("oauth")
public class Oauth2ClientAccessTokenProvider {

    @Resource
    private AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager;

    /** Method to directly get the access token. */
    public String authenticateClientAndGetAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest.withClientRegistrationId(OAUTH_CLIENT_ID)
                        .principal("API")
                        .build();

        // Perform the actual authorization request using the authorized client service
        // and authorized client manager.
        // This is where the token is retrieved from the Oauth providers.
        OAuth2AuthorizedClient authorizedClient =
                this.authorizedClientServiceAndManager.authorize(authorizeRequest);

        // Get the token from the authorized client object
        OAuth2AccessToken accessToken = Objects.requireNonNull(authorizedClient).getAccessToken();
        return accessToken.getTokenValue();
    }
}
