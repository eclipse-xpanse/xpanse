/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions;

/**
 * Exception thrown when OpenTofu Provider configuration for the requested Csp is not available.
 */
public class OpenTofuProviderNotFoundException extends RuntimeException {
    public OpenTofuProviderNotFoundException(String message) {
        super(message);
    }

}

