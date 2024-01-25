/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions;

/**
 * Exception thrown when Terraform Provider configuration for the requested Csp is not available.
 */
public class TerraformProviderNotFoundException extends RuntimeException {
    public TerraformProviderNotFoundException(String message) {
        super(message);
    }

}

