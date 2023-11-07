/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyQueryRequestTest {

    @Mock
    private Csp mockCsp;

    private PolicyQueryRequest policyQueryRequestUnderTest;

    @BeforeEach
    void setUp() {
        policyQueryRequestUnderTest = new PolicyQueryRequest();
        policyQueryRequestUnderTest.setCsp(mockCsp);
    }

    @Test
    void testEnabledGetterAndSetter() {
        final Boolean enabled = false;
        policyQueryRequestUnderTest.setEnabled(enabled);
        assertThat(policyQueryRequestUnderTest.getEnabled()).isFalse();
    }

    @Test
    void testUserIdGetterAndSetter() {
        final String userId = "userId";
        policyQueryRequestUnderTest.setUserId(userId);
        assertThat(policyQueryRequestUnderTest.getUserId()).isEqualTo(userId);
    }

    @Test
    void testGetCsp() {
        assertThat(policyQueryRequestUnderTest.getCsp()).isEqualTo(mockCsp);
    }

    @Test
    void testPolicyGetterAndSetter() {
        final String policy = "policy";
        policyQueryRequestUnderTest.setPolicy(policy);
        assertThat(policyQueryRequestUnderTest.getPolicy()).isEqualTo(policy);
    }

    @Test
    void testEquals() {
        assertThat(policyQueryRequestUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyQueryRequestUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyQueryRequest test1 = new PolicyQueryRequest();
        assertThat(policyQueryRequestUnderTest.hashCode()).isNotEqualTo(test1.hashCode());

        PolicyQueryRequest test2 = new PolicyQueryRequest();
        test2.setCsp(mockCsp);
        assertThat(policyQueryRequestUnderTest.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result = "PolicyQueryRequest(enabled=null, userId=null, csp=mockCsp, policy=null)";
        assertThat(policyQueryRequestUnderTest.toString()).isEqualTo(result);
    }
}
