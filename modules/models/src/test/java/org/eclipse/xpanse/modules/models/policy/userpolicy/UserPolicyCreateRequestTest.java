/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.userpolicy;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class UserPolicyCreateRequestTest {

    @Mock private Csp mockCsp;

    private final Boolean enabled = true;

    private final String policy = "policy";

    private UserPolicyCreateRequest test;

    @BeforeEach
    void setUp() {
        test = new UserPolicyCreateRequest();
        test.setCsp(mockCsp);
        test.setPolicy(policy);
        test.setEnabled(enabled);
    }

    @Test
    void testGetters() {
        assertThat(test.getPolicy()).isEqualTo(policy);
        assertThat(test.getCsp()).isEqualTo(mockCsp);
        assertThat(test.getEnabled()).isEqualTo(enabled);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        UserPolicyCreateRequest test1 = new UserPolicyCreateRequest();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        assertThat(test.hashCode()).isNotEqualTo(new Object().hashCode());

        UserPolicyCreateRequest test1 = new UserPolicyCreateRequest();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String result =
                String.format(
                        "UserPolicyCreateRequest(csp=%s, policy=%s, enabled=%s)",
                        mockCsp, policy, enabled);
        assertThat(test.toString()).isEqualTo(result);
    }
}
