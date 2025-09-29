/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

/**
 * Exception thrown when any of the integrated systems throw a rate limiter exception. When this
 * exception is thrown, we must not retry the calls.
 */
public class RateLimiterException extends RuntimeException {

    public RateLimiterException(String message) {
        super(message);
    }
}
