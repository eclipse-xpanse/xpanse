/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions;

/**
 * Exception thrown when the queried service template request is not found.
 */
public class ServiceTemplateRequestNotFound extends RuntimeException {
    public ServiceTemplateRequestNotFound(String message) {
        super(message);
    }

}

