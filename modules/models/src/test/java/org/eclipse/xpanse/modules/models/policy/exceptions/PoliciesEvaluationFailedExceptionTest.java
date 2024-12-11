/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PoliciesEvaluationFailedExceptionTest {

    private PoliciesEvaluationFailedException policiesEvaluationFailedExceptionUnderTest;

    @BeforeEach
    void setUp() {
        policiesEvaluationFailedExceptionUnderTest =
                new PoliciesEvaluationFailedException("errorReason");
    }

    @Test
    void testGetErrorReason() {
        assertThat(policiesEvaluationFailedExceptionUnderTest.getErrorReason())
                .isEqualTo("errorReason");
    }

    @Test
    void testToString() {
        String result = "PoliciesEvaluationFailedException(errorReason=errorReason)";
        assertThat(policiesEvaluationFailedExceptionUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(policiesEvaluationFailedExceptionUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policiesEvaluationFailedExceptionUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PoliciesEvaluationFailedException test = new PoliciesEvaluationFailedException("error");
        assertThat(policiesEvaluationFailedExceptionUnderTest.hashCode())
                .isNotEqualTo(test.hashCode());
    }
}
