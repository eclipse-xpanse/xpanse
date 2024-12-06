/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.change.exceptions;

/**
 * Exception thrown when the service template change request is not allowed.
 */
public class ServiceTemplateChangeRequestNotAllowed extends RuntimeException {
    public ServiceTemplateChangeRequestNotAllowed(String message) {
        super(message);
    }

}