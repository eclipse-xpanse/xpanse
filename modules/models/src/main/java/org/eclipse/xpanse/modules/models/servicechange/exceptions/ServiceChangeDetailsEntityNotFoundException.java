/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange.exceptions;

/** Exception thrown when service change details request not found. */
public class ServiceChangeDetailsEntityNotFoundException extends RuntimeException {
    public ServiceChangeDetailsEntityNotFoundException(String message) {
        super(message);
    }
}
