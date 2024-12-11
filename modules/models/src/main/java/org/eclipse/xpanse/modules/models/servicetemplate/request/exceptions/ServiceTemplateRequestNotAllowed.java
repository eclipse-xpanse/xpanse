/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions;

/**
 * Exception thrown when the service template request is not allowed.
 */
public class ServiceTemplateRequestNotAllowed extends RuntimeException {
    public ServiceTemplateRequestNotAllowed(String message) {
        super(message);
    }

}