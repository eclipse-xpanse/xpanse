/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/**
 * Exception thrown when the service migration is not found.
 */
public class ServiceMigrationNotFoundException extends RuntimeException {
    public ServiceMigrationNotFoundException(String message) {
        super(message);
    }


}

