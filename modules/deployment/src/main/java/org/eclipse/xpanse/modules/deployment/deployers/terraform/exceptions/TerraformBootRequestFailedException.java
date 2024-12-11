/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions;

/** Exception thrown when Terraform get the hostname of Xpanse service. */
public class TerraformBootRequestFailedException extends RuntimeException {

    public TerraformBootRequestFailedException(String message) {
        super("TFExecutor Exception: " + message);
    }
}
