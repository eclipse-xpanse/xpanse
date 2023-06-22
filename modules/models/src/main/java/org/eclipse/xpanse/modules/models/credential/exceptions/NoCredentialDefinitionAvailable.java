/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;


/**
 * Exception thrown when we try to create a credential of a type that is not
 * supported by the cloud provider.
 */
public class NoCredentialDefinitionAvailable extends RuntimeException {

    public NoCredentialDefinitionAvailable(String message) {
        super(message);
    }
}
