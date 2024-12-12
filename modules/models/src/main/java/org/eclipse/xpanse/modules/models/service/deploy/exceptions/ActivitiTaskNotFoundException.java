/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/** Exception thrown when the during migration, the migration task was not found. */
public class ActivitiTaskNotFoundException extends RuntimeException {
    public ActivitiTaskNotFoundException(String message) {
        super(message);
    }
}
