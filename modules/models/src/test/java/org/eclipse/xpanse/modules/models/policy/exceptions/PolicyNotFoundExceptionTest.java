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

    private PolicyNotFoundException test;

    @BeforeEach
    void setUp() {
        test = new PolicyNotFoundException("errorReason");
    }

    @Test
    void testGetErrorReason() {
        assertThat(test.getErrorReason()).isEqualTo("errorReason");
    }

    @Test
    void testToString() {
        String result = "PolicyNotFoundException(errorReason=errorReason)";
        assertThat(test.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyNotFoundException test1 = new PolicyNotFoundException("error");
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
    }
}
