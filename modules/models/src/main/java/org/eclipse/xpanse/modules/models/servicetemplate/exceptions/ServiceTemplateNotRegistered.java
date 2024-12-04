/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

/**
 * Exception thrown when the deployer mentioned in the service is not available.
 */
public class ServiceTemplateNotRegistered extends RuntimeException {
    public ServiceTemplateNotRegistered(String message) {
        super(message);
    }

}

