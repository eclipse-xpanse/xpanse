/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.userpolicy;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class UserPolicyQueryRequestTest {

    private final Boolean enabled = true;
    private final String policy = "policy";
    private final String userId = "userId";
    @Mock
    private Csp mockCsp;
    private UserPolicyQueryRequest test;

    @BeforeEach
    void setUp() {
        test = new UserPolicyQueryRequest();
        test.setCsp(mockCsp);
        test.setEnabled(enabled);
        test.setUserId(userId);
        test.setPolicy(policy);
    }

    @Test
    void testGetters() {
        assertThat(test.getEnabled()).isEqualTo(enabled);
        assertThat(test.getUserId()).isEqualTo(userId);
        assertThat(test.getCsp()).isEqualTo(mockCsp);
        assertThat(test.getPolicy()).isEqualTo(policy);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        UserPolicyQueryRequest test1 = new UserPolicyQueryRequest();
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
        UserPolicyQueryRequest test1 = new UserPolicyQueryRequest();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String result =
                String.format("UserPolicyQueryRequest(enabled=%s, userId=%s, csp=%s, policy=%s)",
                        enabled, userId, mockCsp, policy);
        assertThat(test.toString()).isEqualTo(result);
    }
}
