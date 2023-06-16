/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.models;

import com.huaweicloud.sdk.core.retry.RetryContext;
import com.huaweicloud.sdk.core.retry.backoff.BackoffStrategy;

/**
 * Define retry strategy.
 */
public record HuaweiCloudRetryStrategy(long delayMillis) implements BackoffStrategy {

    public static final int DEFAULT_RETRY_TIMES = 5;

    public static final long DEFAULT_DELAY_MILLIS = 60000;

    public static final int ERROR_CODE_TOO_MANY_REQUESTS = 429;

    public static final int ERROR_CODE_INTERNAL_SERVER_ERROR = 500;

    @Override
    public <ResT> long computeDelayBeforeNextRetry(RetryContext<ResT> retryContext) {
        return delayMillis;
    }
}
