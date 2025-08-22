/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.common.oauth2.client;

import static org.eclipse.xpanse.common.oauth2.client.Oauth2ClientConfiguration.OAUTH_CLIENT_ID;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final AuthorizedClientServiceOAuth2AuthorizedClientManager
            authorizedClientServiceAndManager;

    @Autowired
    public Oauth2ClientAccessTokenProvider(
            AuthorizedClientServiceOAuth2AuthorizedClientManager
                    authorizedClientServiceAndManager) {
        this.authorizedClientServiceAndManager = authorizedClientServiceAndManager;
    }

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
        if (Objects.nonNull(authorizedClient)) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            return accessToken.getTokenValue();
        } else {
            return null;
        }
    }
}
