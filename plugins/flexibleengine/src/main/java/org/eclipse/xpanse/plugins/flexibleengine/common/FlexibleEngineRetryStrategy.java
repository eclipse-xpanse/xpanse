/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.common;

import com.huaweicloud.sdk.core.SdkResponse;
import com.huaweicloud.sdk.core.exception.ClientRequestException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.core.retry.RetryContext;
import com.huaweicloud.sdk.core.retry.backoff.BackoffStrategy;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.config.HttpClientRequestRetryProperties;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientAuthenticationFailedException;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/** Define retry strategy. */
@Slf4j
@RefreshScope
@Component
@Getter
public class FlexibleEngineRetryStrategy implements BackoffStrategy {

    public static final int WAITING_JOB_SUCCESS_RETRY_TIMES = 30;
    private static final int ERROR_CODE_TOO_MANY_REQUESTS = 429;
    private static final int ERROR_CODE_INTERNAL_SERVER_ERROR = 500;
    private static final int DEFAULT_RETRY_ATTEMPTS = 5;
    private static final long DEFAULT_DELAY_MILLIONS = 30000L;
    private final HttpClientRequestRetryProperties httpClientRequestRetryProperties;

    public FlexibleEngineRetryStrategy(
            HttpClientRequestRetryProperties httpClientRequestRetryProperties) {
        this.httpClientRequestRetryProperties = httpClientRequestRetryProperties;
    }

    @Override
    public <T> long computeDelayBeforeNextRetry(RetryContext<T> retryContext) {
        return getRetryDelayMilliSeconds();
    }

    @Override
    public long computeDelayBeforeNextRetry(int retries) {
        return getRetryDelayMilliSeconds();
    }

    /** Calculates the maximum gap between two retries in case of failures. */
    public long getRetryDelayMilliSeconds() {
        if (httpClientRequestRetryProperties.getDelayMilliseconds() <= 0) {
            log.warn(
                    "The max delay millions time is invalid, use default value {}",
                    DEFAULT_DELAY_MILLIONS);
            return DEFAULT_DELAY_MILLIONS;
        }
        return httpClientRequestRetryProperties.getDelayMilliseconds().longValue();
    }

    /** Calculates the maximum retry attempts to be done for an HTTP request. */
    public int getRetryMaxAttempts() {
        if (httpClientRequestRetryProperties.getRetryMaxAttempts() <= 0) {
            log.warn(
                    "The retry max attempts is invalid, use default value {}",
                    DEFAULT_RETRY_ATTEMPTS);
            return DEFAULT_RETRY_ATTEMPTS;
        }
        return httpClientRequestRetryProperties.getRetryMaxAttempts();
    }

    /**
     * Match retry condition.
     *
     * @param response response
     * @param ex exception
     * @return true if match retry condition, otherwise false
     */
    public boolean matchRetryCondition(SdkResponse response, Exception ex) {
        if (Objects.isNull(ex)) {
            return false;
        }
        if (!ServiceResponseException.class.isAssignableFrom(ex.getClass())) {
            return false;
        }
        int statusCode = ((ServiceResponseException) ex).getHttpStatusCode();
        return statusCode == ERROR_CODE_TOO_MANY_REQUESTS
                || statusCode == ERROR_CODE_INTERNAL_SERVER_ERROR;
    }

    /**
     * Handle auth exception for spring retry.
     *
     * @param ex Exception
     */
    public void handleAuthExceptionForSpringRetry(Exception ex) {
        org.springframework.retry.RetryContext retryContext =
                RetrySynchronizationManager.getContext();
        int retryCount = Objects.isNull(retryContext) ? 0 : retryContext.getRetryCount();
        log.error(ex.getMessage() + System.lineSeparator() + "Retry count:" + retryCount);
        if (ex instanceof ClientAuthenticationFailedException) {
            throw new ClientAuthenticationFailedException(ex.getMessage());
        }
        ClientRequestException clientRequestException = getClientRequestException(ex);
        if (Objects.nonNull(clientRequestException)) {
            int statusCode = clientRequestException.getHttpStatusCode();
            if (statusCode == HttpStatus.UNAUTHORIZED.value()
                    || statusCode == HttpStatus.FORBIDDEN.value()) {
                throw new ClientAuthenticationFailedException(ex.getMessage());
            }
        }
    }

    private ClientRequestException getClientRequestException(Throwable ex) {
        if (Objects.isNull(ex)) {
            return null;
        }
        if (ex instanceof ClientRequestException requestException) {
            return requestException;
        }
        if (Objects.nonNull(ex.getCause())) {
            return getClientRequestException(ex.getCause());
        }
        return null;
    }
}
