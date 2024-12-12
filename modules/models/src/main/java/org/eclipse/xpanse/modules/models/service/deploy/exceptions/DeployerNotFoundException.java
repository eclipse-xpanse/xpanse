/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/** Exception thrown when the deployer mentioned in the service is not available. */
public class DeployerNotFoundException extends RuntimeException {
    public DeployerNotFoundException(String message) {
        super(message);
    }
}
