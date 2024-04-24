package org.eclipse.xpanse.modules.models.policy.servicepolicy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class ServicePolicyUpdateRequestTest {

    final UUID id = UUID.fromString("0ebfecbc-3907-45c7-b9c6-36d42ec0efa1");
    final List<String> flavorNameList = List.of("flavor1","flavor2");
    final String policy = "policy";
    final Boolean enabled = false;
    private ServicePolicyUpdateRequest test;

    @BeforeEach
    void setUp() {
        test = new ServicePolicyUpdateRequest();
        test.setEnabled(enabled);
        test.setFlavorNameList(flavorNameList);
        test.setPolicy(policy);
    }

    @Test
    void testGetters() {
        assertThat(test.getFlavorNameList()).isEqualTo(flavorNameList);
        assertThat(test.getPolicy()).isEqualTo(policy);
        assertThat(test.getEnabled()).isEqualTo(enabled);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        ServicePolicyUpdateRequest test1 = new ServicePolicyUpdateRequest();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        assertThat(test.hashCode() == new Object().hashCode()).isFalse();
        ServicePolicyUpdateRequest test1 = new ServicePolicyUpdateRequest();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result = String.format("ServicePolicyUpdateRequest(flavorNameList=%s, "
                        + "policy=%s, enabled=%s)",
                flavorNameList, policy, enabled);
        assertThat(test.toString()).isEqualTo(result);
    }
}
