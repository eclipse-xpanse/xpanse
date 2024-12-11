/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/**
 * Exception thrown when the flavour provided in the deployment request is not supported by the
 * service.
 */
public class FlavorInvalidException extends RuntimeException {
    public FlavorInvalidException(String message) {
        super(message);
    }
}
