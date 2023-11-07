/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyNotFoundExceptionTest {

    private PolicyNotFoundException policyNotFoundExceptionUnderTest;

    @BeforeEach
    void setUp() {
        policyNotFoundExceptionUnderTest = new PolicyNotFoundException("errorReason");
    }

    @Test
    void testGetErrorReason() {
        assertThat(policyNotFoundExceptionUnderTest.getErrorReason()).isEqualTo("errorReason");
    }

    @Test
    void testToString() {
        String result = "PolicyNotFoundException(errorReason=errorReason)";
        assertThat(policyNotFoundExceptionUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(policyNotFoundExceptionUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyNotFoundExceptionUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyNotFoundException test = new PolicyNotFoundException("error");
        assertThat(policyNotFoundExceptionUnderTest.hashCode()).isNotEqualTo(test.hashCode());
    }
}
