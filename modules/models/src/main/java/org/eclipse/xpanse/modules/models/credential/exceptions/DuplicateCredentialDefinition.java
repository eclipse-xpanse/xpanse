/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;


/**
 * Exception thrown when we defined duplicate CredentialDefinition in the plugin of
 * the cloud provider.
 */
public class DuplicateCredentialDefinition extends RuntimeException {

    public DuplicateCredentialDefinition(String message) {
        super(message);
    }
}
