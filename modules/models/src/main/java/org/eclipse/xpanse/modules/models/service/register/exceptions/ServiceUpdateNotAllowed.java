/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.exceptions;

/**
 * Exception thrown when not allowed fields of a registered service is updated.
 */
public class ServiceUpdateNotAllowed extends RuntimeException {
    public ServiceUpdateNotAllowed(String message) {
        super(message);
    }

}