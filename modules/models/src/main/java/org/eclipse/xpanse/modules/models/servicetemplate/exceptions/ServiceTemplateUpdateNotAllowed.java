/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

/**
 * Exception thrown when not allowed fields of a registered service template is updated.
 */
public class ServiceTemplateUpdateNotAllowed extends RuntimeException {
    public ServiceTemplateUpdateNotAllowed(String message) {
        super(message);
    }

}