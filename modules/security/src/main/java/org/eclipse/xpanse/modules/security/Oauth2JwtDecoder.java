/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import java.time.Duration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Beans helper for creating JwtDecoder for OAuth2 JWT authentication.
 */
@Slf4j
@Configuration
public class Oauth2JwtDecoder {


    /**
     * Create JwtDecoder for OAuth2 JWT authentication.
     *
     * @param issuerUri url of the issuer.
     * @return JwtDecoder.
     */
    @Retryable(retryFor = Exception.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public JwtDecoder createJwtDecoder(String issuerUri) {
        int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
        log.info("Creating Oauth2 JwtDecoder from issuerUri:{}. Retry count:{}", issuerUri,
                retryCount);
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ofSeconds(60)),
                new JwtIssuerValidator(issuerUri));
        jwtDecoder.setJwtValidator(withClockSkew);
        return jwtDecoder;
    }

}
