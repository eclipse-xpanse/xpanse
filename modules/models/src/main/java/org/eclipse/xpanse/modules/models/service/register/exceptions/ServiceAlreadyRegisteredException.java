/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.exceptions;

/**
 * Exception thrown when the deployer mentioned in the service is not available.
 */
public class ServiceAlreadyRegisteredException extends RuntimeException {
    public ServiceAlreadyRegisteredException(String message) {
        super(message);
    }


}

