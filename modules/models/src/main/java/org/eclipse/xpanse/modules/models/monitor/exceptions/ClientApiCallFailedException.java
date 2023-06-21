/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.monitor.exceptions;

/**
 * Exception thrown when calling API by the client.
 */
public class ClientApiCallFailedException extends RuntimeException {

    public ClientApiCallFailedException(String message) {
        super("Client Called API Exception:" + message);
    }
}

