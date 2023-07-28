/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

/**
 * Exception thrown when there is an error during enum deserialization.
 */
public class UnsupportedEnumValueException extends RuntimeException {

    public UnsupportedEnumValueException(String message) {
        super(message);
    }
}