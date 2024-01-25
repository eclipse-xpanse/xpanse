/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions;

/**
 * Defines possible exceptions returned by OpenTofu execution.
 */
public class OpenTofuExecutorException extends RuntimeException {

    public OpenTofuExecutorException(String message) {
        super("OpenTofuExecutor Exception: " + message);
    }

    public OpenTofuExecutorException(String message, Throwable ex) {
        super(message, ex);
    }

    public OpenTofuExecutorException(String message, String output) {
        super("OpenTofuExecutor Exception:" + message + System.lineSeparator() + output);
    }
}
