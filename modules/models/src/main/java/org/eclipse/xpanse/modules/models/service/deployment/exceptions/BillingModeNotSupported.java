/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment.exceptions;

/**
 * Exception thrown when the billing mode selected during service deployment or migration is not
 * supported.
 */
public class BillingModeNotSupported extends RuntimeException {

    public BillingModeNotSupported(String message) {
        super(message);
    }
}
