/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

/** Defines exception when the deployer tool is invalid or installed failed. */
public class InvalidDeployerToolException extends RuntimeException {
    public InvalidDeployerToolException(String message) {
        super(message);
    }
}
