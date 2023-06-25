/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.exceptions;

/**
 * Exception thrown while processing the icon in the registration request.
 */
public class IconProcessingFailedException extends RuntimeException {
    public IconProcessingFailedException(String message) {
        super(message);
    }


}

