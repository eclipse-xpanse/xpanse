/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyUpdateRequestTest {

    @Mock
    private Csp mockCsp;

    private PolicyUpdateRequest policyUpdateRequestUnderTest;

    @BeforeEach
    void setUp() {
        policyUpdateRequestUnderTest = new PolicyUpdateRequest();
        policyUpdateRequestUnderTest.setCsp(mockCsp);
    }

    @Test
    void testIdGetterAndSetter() {
        final UUID id = UUID.fromString("8f06ca43-e699-424e-92d4-37b85f35134f");
        policyUpdateRequestUnderTest.setId(id);
        assertThat(policyUpdateRequestUnderTest.getId()).isEqualTo(id);
    }

    @Test
    void testGetCsp() {
        assertThat(policyUpdateRequestUnderTest.getCsp()).isEqualTo(mockCsp);
    }

    @Test
    void testPolicyGetterAndSetter() {
        final String policy = "policy";
        policyUpdateRequestUnderTest.setPolicy(policy);
        assertThat(policyUpdateRequestUnderTest.getPolicy()).isEqualTo(policy);
    }

    @Test
    void testEnabledGetterAndSetter() {
        final Boolean enabled = false;
        policyUpdateRequestUnderTest.setEnabled(enabled);
        assertThat(policyUpdateRequestUnderTest.getEnabled()).isFalse();
    }

    @Test
    void testEquals() {
        assertThat(policyUpdateRequestUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyUpdateRequestUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyUpdateRequest test1 = new PolicyUpdateRequest();
        assertThat(policyUpdateRequestUnderTest.hashCode()).isNotEqualTo(test1.hashCode());
        PolicyUpdateRequest test2 = new PolicyUpdateRequest();
        test2.setCsp(mockCsp);
        assertThat(policyUpdateRequestUnderTest.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "PolicyUpdateRequest(id=null, csp=mockCsp, policy=null, enabled=null)";
        assertThat(policyUpdateRequestUnderTest.toString()).isEqualTo(result);
    }
}
