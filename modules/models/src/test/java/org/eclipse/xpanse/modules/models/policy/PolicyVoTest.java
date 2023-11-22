/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyVoTest {

    @Mock
    private Csp mockCsp;

    private Policy policyUnderTest;

    @BeforeEach
    void setUp() {
        policyUnderTest = new Policy();
        policyUnderTest.setCsp(mockCsp);
    }

    @Test
    void testIdGetterAndSetter() {
        final UUID id = UUID.fromString("34da927b-3ce0-4aef-85fb-aa2e87d9a724");
        policyUnderTest.setId(id);
        assertThat(policyUnderTest.getId()).isEqualTo(id);
    }

    @Test
    void testPolicyGetterAndSetter() {
        final String policy = "policy";
        policyUnderTest.setPolicy(policy);
        assertThat(policyUnderTest.getPolicy()).isEqualTo(policy);
    }

    @Test
    void testGetCsp() {
        assertThat(policyUnderTest.getCsp()).isEqualTo(mockCsp);
    }

    @Test
    void testEnabledGetterAndSetter() {
        final Boolean enabled = false;
        policyUnderTest.setEnabled(enabled);
        assertThat(policyUnderTest.getEnabled()).isFalse();
    }

    @Test
    void testCreateTimeGetterAndSetter() {
        final OffsetDateTime createTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        policyUnderTest.setCreateTime(createTime);
        assertThat(policyUnderTest.getCreateTime()).isEqualTo(createTime);
    }

    @Test
    void testLastModifiedTimeGetterAndSetter() {
        final OffsetDateTime lastModifiedTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        policyUnderTest.setLastModifiedTime(lastModifiedTime);
        assertThat(policyUnderTest.getLastModifiedTime()).isEqualTo(lastModifiedTime);
    }

    @Test
    void testEquals() {
        assertThat(policyUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        Policy test1 = new Policy();
        assertThat(policyUnderTest.hashCode()).isNotEqualTo(test1.hashCode());
        Policy test2 = new Policy();
        test2.setCsp(mockCsp);
        assertThat(policyUnderTest.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result = "Policy(id=null, policy=null, csp=mockCsp, enabled=null," +
                " createTime=null, lastModifiedTime=null)";
        assertThat(policyUnderTest.toString()).isEqualTo(result);
    }
}
