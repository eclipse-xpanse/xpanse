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
class PolicyCreateRequestTest {

    @Mock
    private Csp mockCsp;

    private PolicyCreateRequest policyCreateRequestUnderTest;

    @BeforeEach
    void setUp() {
        policyCreateRequestUnderTest = new PolicyCreateRequest();
        policyCreateRequestUnderTest.setCsp(mockCsp);
    }

    @Test
    void testUserIdGetterAndSetter() {
        final String userId = "userId";
        policyCreateRequestUnderTest.setUserId(userId);
        assertThat(policyCreateRequestUnderTest.getUserId()).isEqualTo(userId);
    }

    @Test
    void testGetCsp() {
        assertThat(policyCreateRequestUnderTest.getCsp()).isEqualTo(mockCsp);
    }

    @Test
    void testPolicyGetterAndSetter() {
        final String policy = "policy";
        policyCreateRequestUnderTest.setPolicy(policy);
        assertThat(policyCreateRequestUnderTest.getPolicy()).isEqualTo(policy);
    }

    @Test
    void testEnabledGetterAndSetter() {
        final Boolean enabled = false;
        policyCreateRequestUnderTest.setEnabled(enabled);
        assertThat(policyCreateRequestUnderTest.getEnabled()).isFalse();
    }

    @Test
    void testEquals() {
        assertThat(policyCreateRequestUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyCreateRequestUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyCreateRequest request2 = new PolicyCreateRequest();
        request2.setCsp(mockCsp);
        assertThat(policyCreateRequestUnderTest.hashCode()).isEqualTo(request2.hashCode());

        PolicyCreateRequest request3 = new PolicyCreateRequest();
        assertThat(policyCreateRequestUnderTest.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    void testToString() {
        String result = "PolicyCreateRequest(userId=null, csp=mockCsp, policy=null, enabled=null)";
        assertThat(policyCreateRequestUnderTest.toString()).isEqualTo(result);
    }
}
