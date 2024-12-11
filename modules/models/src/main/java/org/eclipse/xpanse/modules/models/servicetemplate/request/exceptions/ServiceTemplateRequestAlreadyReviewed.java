/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions;

/**
 * Exception thrown when the service template request is already reviewed.
 */
public class ServiceTemplateRequestAlreadyReviewed extends RuntimeException {
    public ServiceTemplateRequestAlreadyReviewed(String message) {
        super(message);
    }

}