/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment.exceptions;

/** Exception thrown when the service recreate is not found. */
public class ServiceRecreateOrderNotFoundException extends RuntimeException {
    public ServiceRecreateOrderNotFoundException(String message) {
        super(message);
    }
}
