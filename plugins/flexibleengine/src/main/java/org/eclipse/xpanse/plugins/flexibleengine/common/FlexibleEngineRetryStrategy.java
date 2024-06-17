/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.common;

import com.huaweicloud.sdk.core.SdkResponse;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.core.retry.RetryContext;
import com.huaweicloud.sdk.core.retry.backoff.BackoffStrategy;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Define retry strategy.
 */
@Slf4j
@Component
public class FlexibleEngineRetryStrategy implements BackoffStrategy {

    public static final int WAITING_JOB_SUCCESS_RETRY_TIMES = 30;
    private static final int ERROR_CODE_TOO_MANY_REQUESTS = 429;
    private static final int ERROR_CODE_INTERNAL_SERVER_ERROR = 500;
    private static final int DEFAULT_RETRY_ATTEMPTS = 5;
    private static final long DEFAULT_DELAY_MILLIONS = 30000L;
    private static int retryMaxAttempts;
    private static long retryMaxDelayMillions;

    @Override
    public <T> long computeDelayBeforeNextRetry(RetryContext<T> retryContext) {
        return retryMaxDelayMillions;
    }


    /**
     * Set retry max attempts with config.
     *
     * @param maxAttempts retry max attempts.
     */
    @Value("${http.request.retry.max.attempts:5}")
    public void setRetryAttempts(int maxAttempts) {
        if (maxAttempts <= 0) {
            retryMaxAttempts = DEFAULT_RETRY_ATTEMPTS;
            log.warn("The retry max attempts is invalid, use default value {}",
                    DEFAULT_RETRY_ATTEMPTS);
        }
        retryMaxAttempts = maxAttempts;
    }

    /**
     * Set max delay millions time with config.
     *
     * @param delayMillions max delay millions time.
     */
    @Value("${http.request.retry.delay.milliseconds:30000}")
    public void setRetryDelayMillions(long delayMillions) {
        if (delayMillions <= 0) {
            retryMaxDelayMillions = DEFAULT_DELAY_MILLIONS;
            log.warn("The max delay millions time is invalid, use default value {}",
                    DEFAULT_DELAY_MILLIONS);
        }
        retryMaxDelayMillions = delayMillions;
    }

    /**
     * Get retry max attempts.
     *
     * @return retry max attempts.
     */
    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    /**
     * Match retry condition.
     *
     * @param response response
     * @param ex       exception
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
}
