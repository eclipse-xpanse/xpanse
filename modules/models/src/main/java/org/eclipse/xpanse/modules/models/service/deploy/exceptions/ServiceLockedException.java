/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/** Exception thrown when the service is locked for modification or destruction. */
public class ServiceLockedException extends RuntimeException {
    public ServiceLockedException(String message) {
        super(message);
    }
}
