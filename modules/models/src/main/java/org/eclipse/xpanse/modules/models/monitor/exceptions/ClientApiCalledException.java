/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.monitor.exceptions;

/**
 * Exception thrown when calling API by the client.
 */
public class ClientApiCalledException extends RuntimeException {

    public ClientApiCalledException(String message) {
        super("Client Called API Exception:" + message);
    }
}

