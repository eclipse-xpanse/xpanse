/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DuplicateCredentialDefinitionTest {

    private DuplicateCredentialDefinition duplicateCredentialDefinitionUnderTest;

    @BeforeEach
    void setUp() {
        duplicateCredentialDefinitionUnderTest = new DuplicateCredentialDefinition("message");
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals("message", duplicateCredentialDefinitionUnderTest.getMessage());
    }
}
