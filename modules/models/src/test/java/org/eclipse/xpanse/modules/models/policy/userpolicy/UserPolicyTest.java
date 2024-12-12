/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy.userpolicy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class UserPolicyTest {

    @Mock private Csp mockCsp;

    private final Boolean enabled = true;
    private final String policy = "policy";
    private final UUID id = UUID.fromString("8f06ca43-e699-424e-92d4-37b85f35134f");
    private UserPolicy test;

    @BeforeEach
    void setUp() {
        test = new UserPolicy();
        test.setCsp(mockCsp);
        test.setEnabled(enabled);
        test.setPolicy(policy);
        test.setUserPolicyId(id);
    }

    @Test
    void testGetters() {
        assertThat(test.getUserPolicyId()).isEqualTo(id);
        assertThat(test.getPolicy()).isEqualTo(policy);
        assertThat(test.getCsp()).isEqualTo(mockCsp);
        assertThat(test.getEnabled()).isEqualTo(enabled);
    }

    @Test
    void testCreateTimeGetterAndSetter() {
        final OffsetDateTime createTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        test.setCreateTime(createTime);
        assertThat(test.getCreateTime()).isEqualTo(createTime);
    }

    @Test
    void testLastModifiedTimeGetterAndSetter() {
        final OffsetDateTime lastModifiedTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        test.setLastModifiedTime(lastModifiedTime);
        assertThat(test.getLastModifiedTime()).isEqualTo(lastModifiedTime);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        UserPolicy test1 = new UserPolicy();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        UserPolicy test1 = new UserPolicy();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        UserPolicy test2 = new UserPolicy();
        BeanUtils.copyProperties(test, test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                String.format(
                        "UserPolicy(userPolicyId=%s, policy=%s, csp=%s, enabled=%s, createTime=%s,"
                                + " lastModifiedTime=%s)",
                        id, policy, mockCsp, enabled, null, null);
        assertThat(test.toString()).isEqualTo(result);
    }
}
