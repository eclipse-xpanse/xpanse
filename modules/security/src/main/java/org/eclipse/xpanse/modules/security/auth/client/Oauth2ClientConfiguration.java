/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.security.auth.client;

import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Bean to create an authorized client which can be used by any other method within the application
 * to fetch authorization tokens.
 */
@Configuration
@Profile("oauth")
public class Oauth2ClientConfiguration {

    public static final String OAUTH_CLIENT_ID = "oauth2";
    private static final String[] DEFAULT_SCOPES = {"openid", "profile"};
    private final SecurityProperties securityProperties;

    /** Constructor method. */
    public Oauth2ClientConfiguration(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    // Create the Oauth client registration
    @Bean
    ClientRegistration oauthClientRegistration() {
        return ClientRegistration.withRegistrationId(OAUTH_CLIENT_ID)
                .tokenUri(securityProperties.getOauth().getTokenUrl())
                .clientId(securityProperties.getOauth().getClient().getClientId())
                .clientSecret(securityProperties.getOauth().getClient().getClientSecret())
                .scope(DEFAULT_SCOPES)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

    // Create the client registration repository
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            ClientRegistration clientRegistration) {
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    // Create the authorized client service
    @Bean
    public OAuth2AuthorizedClientService auth2AuthorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * Create the authorized client manager and service manager using the beans created and
     * configured above.
     */
    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
