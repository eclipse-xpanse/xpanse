package org.eclipse.xpanse.modules.database.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyEntityTest {

    @Mock
    private Csp mockCsp;

    private PolicyEntity policyEntityUnderTest;

    @BeforeEach
    void setUp() {
        policyEntityUnderTest = new PolicyEntity();
        policyEntityUnderTest.setCsp(mockCsp);
        policyEntityUnderTest.setEnabled(true);
    }

    @Test
    void testIdGetterAndSetter() {
        final UUID id = UUID.fromString("7a3ffeac-b827-45f0-97c8-9299d90345a7");
        policyEntityUnderTest.setId(id);
        assertThat(policyEntityUnderTest.getId()).isEqualTo(id);
    }

    @Test
    void testUserIdGetterAndSetter() {
        final String userId = "userId";
        policyEntityUnderTest.setUserId(userId);
        assertThat(policyEntityUnderTest.getUserId()).isEqualTo(userId);
    }

    @Test
    void testPolicyGetterAndSetter() {
        final String policy = "policy";
        policyEntityUnderTest.setPolicy(policy);
        assertThat(policyEntityUnderTest.getPolicy()).isEqualTo(policy);
    }

    @Test
    void testGetCsp() {
        assertThat(policyEntityUnderTest.getCsp()).isEqualTo(mockCsp);
    }

    @Test
    void testEnabledGetterAndSetter() {
        final Boolean enabled = false;
        policyEntityUnderTest.setEnabled(enabled);
        assertThat(policyEntityUnderTest.getEnabled()).isFalse();
    }

    @Test
    void testToString() {
        String result =
                "PolicyEntity(id=null, userId=null, policy=null, csp=mockCsp, enabled=true)";
        assertThat(policyEntityUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(policyEntityUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(policyEntityUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        PolicyEntity test = new PolicyEntity();
        test.setCsp(mockCsp);
        test.setEnabled(true);
        assertThat(policyEntityUnderTest.hashCode()).isEqualTo(test.hashCode());
    }
}
