/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions;

/** Exception thrown when the review service template request is not allowed . */
public class ReviewServiceTemplateRequestNotAllowed extends RuntimeException {
    public ReviewServiceTemplateRequestNotAllowed(String message) {
        super(message);
    }
}
