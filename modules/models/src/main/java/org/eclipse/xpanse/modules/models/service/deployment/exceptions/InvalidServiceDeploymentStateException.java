/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment.exceptions;

/** Exception thrown when an action on a service is requested during invalid deployment state. */
public class InvalidServiceDeploymentStateException extends RuntimeException {
    public InvalidServiceDeploymentStateException(String message) {
        super(message);
    }
}
