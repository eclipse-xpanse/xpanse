/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/**
 * Exception thrown when Terraform get the hostname of Xpanse service.
 */
public class TerraformBootRequestFailedException extends RuntimeException {

    public TerraformBootRequestFailedException(String message) {
        super("TFExecutor Exception: " + message);
    }
}
