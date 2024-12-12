/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.userpolicy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class UserPolicyUpdateRequestTest {

    private final Boolean enabled = true;
    private final String policy = "policy";
    private final UUID id = UUID.fromString("8f06ca43-e699-424e-92d4-37b85f35134f");
    @Mock private Csp mockCsp;
    private UserPolicyUpdateRequest test;

    @BeforeEach
    void setUp() {
        test = new UserPolicyUpdateRequest();
        test.setCsp(mockCsp);
        test.setEnabled(enabled);
        test.setPolicy(policy);
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
        UserPolicyUpdateRequest test1 = new UserPolicyUpdateRequest();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        UserPolicyUpdateRequest test1 = new UserPolicyUpdateRequest();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        UserPolicyUpdateRequest test2 = new UserPolicyUpdateRequest();
        BeanUtils.copyProperties(test, test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                String.format(
                        "UserPolicyUpdateRequest(csp=%s, policy=%s, enabled=%s)",
                        mockCsp, policy, enabled);
        assertThat(test.toString()).isEqualTo(result);
    }
}
