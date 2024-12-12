/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

/** Exception thrown for errors generated while generating open api file. */
public class OpenApiFileGenerationException extends RuntimeException {
    public OpenApiFileGenerationException(String message) {
        super(message);
    }
}
