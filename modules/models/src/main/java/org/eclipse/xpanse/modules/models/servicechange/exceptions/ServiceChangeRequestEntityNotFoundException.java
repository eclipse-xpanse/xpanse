/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange.exceptions;

/** Exception thrown when service change request entity not found. */
public class ServiceChangeRequestEntityNotFoundException extends RuntimeException {
    public ServiceChangeRequestEntityNotFoundException(String message) {
        super(message);
    }
}
