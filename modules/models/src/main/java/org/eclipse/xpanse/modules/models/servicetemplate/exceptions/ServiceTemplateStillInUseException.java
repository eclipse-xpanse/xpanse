/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

/**
 * Exception thrown when delete service template which is still in use.
 */
public class ServiceTemplateStillInUseException extends RuntimeException {
    public ServiceTemplateStillInUseException(String message) {
        super(message);
    }

}