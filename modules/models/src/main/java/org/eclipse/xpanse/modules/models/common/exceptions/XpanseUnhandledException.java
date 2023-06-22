/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;


/**
 * Exception thrown for errors that are not handled by the application.
 */
public class XpanseUnhandledException extends RuntimeException {
    public XpanseUnhandledException(String message) {
        super(message);
    }
}
