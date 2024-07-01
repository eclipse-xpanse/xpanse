/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.exceptions;

/**
 * Defines exception for deployment failure.
 */
public class DeploymentFailedException extends RuntimeException {

    public DeploymentFailedException(String message) {
        super(message);
    }
}
