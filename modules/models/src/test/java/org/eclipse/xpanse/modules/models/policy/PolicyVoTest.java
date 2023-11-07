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

    private PolicyVo policyVoUnderTest;

    @BeforeEach
    void setUp() {
        policyVoUnderTest = new PolicyVo();
        policyVoUnderTest.setCsp(mockCsp);
    }

    @Test
    void testIdGetterAndSetter() {
        final UUID id = UUID.fromString("34da927b-3ce0-4aef-85fb-aa2e87d9a724");
        policyVoUnderTest.setId(id);
        assertThat(policyVoUnderTest.getId()).isEqualTo(id);
    }

    @Test
    void testPolicyGetterAndSetter() {
        final String policy = "policy";
        policyVoUnderTest.setPolicy(policy);
        assertThat(policyVoUnderTest.getPolicy()).isEqualTo(policy);
    }

    @Test
    void testGetCsp() {
        assertThat(policyVoUnderTest.getCsp()).isEqualTo(mockCsp);
    }

    @Test
    void testEnabledGetterAndSetter() {
        final Boolean enabled = false;
        policyVoUnderTest.setEnabled(enabled);
        assertThat(policyVoUnderTest.getEnabled()).isFalse();
    }

    @Test
    void testCreateTimeGetterAndSetter() {
        final OffsetDateTime createTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        policyVoUnderTest.setCreateTime(createTime);
        assertThat(policyVoUnderTest.getCreateTime()).isEqualTo(createTime);
    }

    @Test
    void testLastModifiedTimeGetterAndSetter() {
        final OffsetDateTime lastModifiedTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        policyVoUnderTest.setLastModifiedTime(lastModifiedTime);
        assertThat(policyVoUnderTest.getLastModifiedTime()).isEqualTo(lastModifiedTime);
    }

    @Test
    void testEquals() {
        assertThat(policyVoUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyVoUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyVo test1 = new PolicyVo();
        assertThat(policyVoUnderTest.hashCode()).isNotEqualTo(test1.hashCode());
        PolicyVo test2 = new PolicyVo();
        test2.setCsp(mockCsp);
        assertThat(policyVoUnderTest.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result = "PolicyVo(id=null, policy=null, csp=mockCsp, enabled=null," +
                " createTime=null, lastModifiedTime=null)";
        assertThat(policyVoUnderTest.toString()).isEqualTo(result);
    }
}
