/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions;

/**
 * Exception thrown when service configuration not found.
 */
public class ServiceConfigurationNotFoundException extends RuntimeException {
    public ServiceConfigurationNotFoundException(String message) {
        super(message);
    }
}
