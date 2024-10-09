/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions;

/**
 * Exception thrown when service configuration update request not found.
 */
public class ServiceConfigurationChangeDetailsEntityNotFoundException extends RuntimeException {
    public ServiceConfigurationChangeDetailsEntityNotFoundException(String message) {
        super(message);
    }
}
