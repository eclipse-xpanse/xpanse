/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

/**
 * An exception is thrown when the encryption and decryption of sensitive fields fail.
 */
public class SensitiveFieldEncryptionOrDecryptionFailedException extends RuntimeException {
    public SensitiveFieldEncryptionOrDecryptionFailedException(String message) {
        super(message);
    }


}

