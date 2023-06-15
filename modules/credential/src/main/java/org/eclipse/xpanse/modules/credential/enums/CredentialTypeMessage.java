/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential.enums;

/**
 * The credential type message.
 */
public enum CredentialTypeMessage {
    VARIABLES_MESSAGE(CredentialType.VARIABLES,
            "value to be provided by creating credential or adding environment variables."),
    HTTP_AUTHENTICATION_MESSAGE(CredentialType.HTTP_AUTHENTICATION, null),
    API_KEY_MESSAGE(CredentialType.API_KEY, null),
    OAUTH2_MESSAGE(CredentialType.OAUTH2, null);

    private final String message;

    private final CredentialType type;

    CredentialTypeMessage(CredentialType type, String message) {
        this.type = type;
        this.message = message;
    }

    /**
     * Get message by type.
     */
    public static String getMessageByType(CredentialType type) {
        for (CredentialTypeMessage typeMessage : values()) {
            if (typeMessage.type == type) {
                return typeMessage.message;
            }
        }
        return null;
    }

}
