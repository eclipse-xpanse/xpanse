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

    private PolicyDuplicateException test;

    @BeforeEach
    void setUp() {
        test = new PolicyDuplicateException("errorReason");
    }

    @Test
    void testGetErrorReason() {
        assertThat(test.getErrorReason()).isEqualTo("errorReason");
    }

    @Test
    void testToString() {
        String result = "PolicyDuplicateException(errorReason=errorReason)";
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
        PolicyDuplicateException test1 = new PolicyDuplicateException("error");
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
    }
}
