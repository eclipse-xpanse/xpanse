/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyDuplicateExceptionTest {

    private PolicyDuplicateException policyDuplicateExceptionUnderTest;

    @BeforeEach
    void setUp() {
        policyDuplicateExceptionUnderTest = new PolicyDuplicateException("errorReason");
    }

    @Test
    void testGetErrorReason() {
        assertThat(policyDuplicateExceptionUnderTest.getErrorReason()).isEqualTo("errorReason");
    }

    @Test
    void testToString() {
        String result = "PolicyDuplicateException(errorReason=errorReason)";
        assertThat(policyDuplicateExceptionUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(policyDuplicateExceptionUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyDuplicateExceptionUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyDuplicateException test = new PolicyDuplicateException("error");
        assertThat(policyDuplicateExceptionUnderTest.hashCode()).isNotEqualTo(test.hashCode());
    }
}
