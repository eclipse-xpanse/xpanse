/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test of CredentialTypeMessage.
 */
class CredentialTypeMessageTest {

    @Test
    public void testGetMessageByType_VariablesMessage() {
        String message = CredentialTypeMessage.getMessageByType(CredentialType.VARIABLES);
        assertEquals("value to be provided by creating credential or adding environment variables.",
                message);
    }

    @Test
    public void testGetMessageByType_HttpAuthenticationMessage() {
        String message = CredentialTypeMessage.getMessageByType(CredentialType.HTTP_AUTHENTICATION);
        assertNull(null, message);
    }

    @Test
    public void testGetMessageByType_ApiKeyMessage() {
        String message = CredentialTypeMessage.getMessageByType(CredentialType.API_KEY);
        assertNull(null, message);
    }

    @Test
    public void testGetMessageByType_OAuth2Message() {
        String message = CredentialTypeMessage.getMessageByType(CredentialType.OAUTH2);
        assertNull(null, message);
    }
}
