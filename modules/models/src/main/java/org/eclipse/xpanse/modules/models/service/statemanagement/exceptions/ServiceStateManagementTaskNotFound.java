/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.statemanagement.exceptions;

/** Exception thrown when the service state management task is not found. */
public final class ServiceStateManagementTaskNotFound extends RuntimeException {
    public ServiceStateManagementTaskNotFound(String message) {
        super(message);
    }
}
