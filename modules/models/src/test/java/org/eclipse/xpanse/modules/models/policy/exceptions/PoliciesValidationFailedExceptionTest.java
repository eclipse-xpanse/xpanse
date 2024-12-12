/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PoliciesValidationFailedExceptionTest {

    private PoliciesValidationFailedException policiesValidationFailedExceptionUnderTest;

    @BeforeEach
    void setUp() {
        policiesValidationFailedExceptionUnderTest =
                new PoliciesValidationFailedException("errorReason");
    }

    @Test
    void testGetErrorReason() {
        assertThat(policiesValidationFailedExceptionUnderTest.getErrorReason())
                .isEqualTo("errorReason");
    }

    @Test
    void testToString() {
        String result = "PoliciesValidationFailedException(errorReason=errorReason)";
        assertThat(policiesValidationFailedExceptionUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(policiesValidationFailedExceptionUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policiesValidationFailedExceptionUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PoliciesValidationFailedException test = new PoliciesValidationFailedException("error");
        assertThat(policiesValidationFailedExceptionUnderTest.hashCode())
                .isNotEqualTo(test.hashCode());
    }
}
