/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/**
 * Exception thrown when the deployment variable is invalid or missing.
 */
public class InvalidDeploymentVariableException extends RuntimeException {
    public InvalidDeploymentVariableException(String message) {
        super(message);
    }


}

