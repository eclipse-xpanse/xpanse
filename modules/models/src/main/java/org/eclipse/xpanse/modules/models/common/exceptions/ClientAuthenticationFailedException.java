/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

/** Exception thrown when the api client authentication failed. */
public class ClientAuthenticationFailedException extends RuntimeException {
    public ClientAuthenticationFailedException(String message) {
        super(message);
    }
}
