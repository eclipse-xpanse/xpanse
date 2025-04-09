/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceobject.exceptions;

/** Exception thrown when service configuration not found. */
public class ServiceObjectNotFoundException extends RuntimeException {
    public ServiceObjectNotFoundException(String message) {
        super(message);
    }
}
