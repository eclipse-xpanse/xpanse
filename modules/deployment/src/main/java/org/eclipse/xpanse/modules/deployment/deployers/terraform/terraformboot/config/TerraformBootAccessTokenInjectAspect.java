/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.config;

import jakarta.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.eclipse.xpanse.common.oauth2.client.Oauth2ClientAccessTokenProvider;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.springframework.stereotype.Component;

/** Aspect for inserting bearer token implicitly to all API calls. */
@Slf4j
@Aspect
@Component
public class TerraformBootAccessTokenInjectAspect {

    private final Oauth2ClientAccessTokenProvider oauth2ClientAccessTokenProvider;

    public TerraformBootAccessTokenInjectAspect(
            @Nullable Oauth2ClientAccessTokenProvider oauth2ClientAccessTokenProvider) {
        this.oauth2ClientAccessTokenProvider = oauth2ClientAccessTokenProvider;
    }

    /**
     * This method automatically catches all calls to the methods in the API package and insert
     * token to the request header.
     */
    // CHECKSTYLE OFF: LineLength
    @Before(
            "execution(*"
                + " org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api..*(..))")
    public void insertOauthToken(JoinPoint joinPoint)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object targetObject = joinPoint.getTarget();
        Method m = targetObject.getClass().getMethod("getApiClient");
        ApiClient apiClient = (ApiClient) m.invoke(targetObject);
        if (Objects.nonNull(oauth2ClientAccessTokenProvider)) {
            log.debug("inserting oauth token to terraform-boot request");
            apiClient.setAccessToken(
                    oauth2ClientAccessTokenProvider.authenticateClientAndGetAccessToken());
        }
    }
}
