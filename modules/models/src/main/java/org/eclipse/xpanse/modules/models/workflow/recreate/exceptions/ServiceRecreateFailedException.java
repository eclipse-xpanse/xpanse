/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow.recreate.exceptions;

/** Exception thrown when the service Recreate was not requested. */
public class ServiceRecreateFailedException extends RuntimeException {
    public ServiceRecreateFailedException(String message) {
        super(message);
    }
}
