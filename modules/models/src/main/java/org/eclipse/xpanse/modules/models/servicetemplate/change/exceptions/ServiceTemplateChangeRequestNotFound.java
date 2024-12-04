/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.change.exceptions;

/**
 * Exception thrown when the queried service template change request is not found.
 */
public class ServiceTemplateChangeRequestNotFound extends RuntimeException {
    public ServiceTemplateChangeRequestNotFound(String message) {
        super(message);
    }

}

