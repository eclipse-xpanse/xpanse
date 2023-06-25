/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.exceptions;

/**
 * Exception thrown when the deployer mentioned in the service is not available.
 */
public class ServiceNotRegisteredException extends RuntimeException {
    public ServiceNotRegisteredException(String message) {
        super(message);
    }


}

