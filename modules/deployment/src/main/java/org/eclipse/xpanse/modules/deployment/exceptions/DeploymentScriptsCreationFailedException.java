/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.exceptions;

/** Defines possible exceptions when the deployment scripts are not created successfully. */
public class DeploymentScriptsCreationFailedException extends RuntimeException {

    public DeploymentScriptsCreationFailedException(String message) {
        super(message);
    }
}
