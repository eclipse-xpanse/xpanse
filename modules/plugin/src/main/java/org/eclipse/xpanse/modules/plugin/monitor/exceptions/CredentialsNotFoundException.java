/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.plugin.monitor.exceptions;

/**
 * Exception thrown when no credentials for connecting to a cloud provider is found.
 */
public class CredentialsNotFoundException extends RuntimeException {
    public CredentialsNotFoundException(String message) {
        super("Credentials not found Exception:" + message);
    }


}

