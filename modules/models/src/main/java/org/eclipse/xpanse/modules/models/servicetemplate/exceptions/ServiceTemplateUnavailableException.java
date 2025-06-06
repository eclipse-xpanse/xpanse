/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

/** Exception thrown when the deployer mentioned in the service template is not available. */
public class ServiceTemplateUnavailableException extends RuntimeException {
    public ServiceTemplateUnavailableException(String message) {
        super(message);
    }
}
