/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment.exceptions;

/** Exception thrown when the service porting is not found. */
public class ServicePortingNotFoundException extends RuntimeException {
    public ServicePortingNotFoundException(String message) {
        super(message);
    }
}
