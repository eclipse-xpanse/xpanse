/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CredentialVariablesNotCompleteTest {

    @Mock
    private Set<String> mockErrorReasons;

    private CredentialVariablesNotComplete credentialVariablesNotCompleteUnderTest;

    @BeforeEach
    void setUp() {
        credentialVariablesNotCompleteUnderTest =
                new CredentialVariablesNotComplete(mockErrorReasons);
    }

    @Test
    void testGetErrorReasons() {
        assertThat(credentialVariablesNotCompleteUnderTest.getErrorReasons())
                .isEqualTo(mockErrorReasons);
    }

    @Test
    void testToString() {
        String result = "CredentialVariablesNotComplete(errorReasons=mockErrorReasons)";
        assertThat(credentialVariablesNotCompleteUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(credentialVariablesNotCompleteUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(credentialVariablesNotCompleteUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        CredentialVariablesNotComplete test = new CredentialVariablesNotComplete(new HashSet<>());
        assertThat(credentialVariablesNotCompleteUnderTest.hashCode()).isNotEqualTo(
                test.hashCode());
    }
}
