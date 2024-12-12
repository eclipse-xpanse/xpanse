/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/** Exception thrown when the service deployment was not requested. */
public class ServiceNotDeployedException extends RuntimeException {
    public ServiceNotDeployedException(String message) {
        super(message);
    }
}
