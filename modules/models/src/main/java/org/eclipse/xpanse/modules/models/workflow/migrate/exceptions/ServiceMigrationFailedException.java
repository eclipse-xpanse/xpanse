/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow.migrate.exceptions;

/** Exception thrown when the service Migration was not requested. */
public class ServiceMigrationFailedException extends RuntimeException {
    public ServiceMigrationFailedException(String message) {
        super(message);
    }
}
