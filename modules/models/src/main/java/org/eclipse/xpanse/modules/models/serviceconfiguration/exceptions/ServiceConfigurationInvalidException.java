/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions;

/**
 * Exception thrown when service configuration is invalid.
 */

public class ServiceConfigurationInvalidException extends RuntimeException {

    public ServiceConfigurationInvalidException(String message) {
        super(message);
    }
}
