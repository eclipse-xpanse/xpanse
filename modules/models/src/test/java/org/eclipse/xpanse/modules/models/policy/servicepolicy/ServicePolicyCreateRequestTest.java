package org.eclipse.xpanse.modules.models.policy.servicepolicy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class ServicePolicyCreateRequestTest {

    final UUID serviceTemplateId = UUID.fromString("3a74cadd-d55b-4504-8fdf-56f1b6aae79c");
    final List<String> flavorNameList = List.of("flavor1", "flavor2");
    final String policy = "policy";
    final Boolean enabled = false;
    private ServicePolicyCreateRequest test;

    @BeforeEach
    void setUp() {
        test = new ServicePolicyCreateRequest();
        test.setServiceTemplateId(serviceTemplateId);
        test.setFlavorNameList(flavorNameList);
        test.setEnabled(enabled);
        test.setPolicy(policy);
    }

    @Test
    void testGetters() {
        assertThat(test.getPolicy()).isEqualTo(policy);
        assertThat(test.getServiceTemplateId()).isEqualTo(serviceTemplateId);
        assertThat(test.getFlavorNameList()).isEqualTo(flavorNameList);
        assertThat(test.getEnabled()).isEqualTo(enabled);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        ServicePolicyCreateRequest test1 = new ServicePolicyCreateRequest();
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
        ServicePolicyCreateRequest test1 = new ServicePolicyCreateRequest();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result = String.format(
                "ServicePolicyCreateRequest(serviceTemplateId=%s, flavorNameList=%s, policy=%s, "
                        + "enabled=%s)", serviceTemplateId, flavorNameList, policy, enabled);
        assertThat(test.toString()).isEqualTo(result);
    }
}
