package org.eclipse.xpanse.modules.models.policy.servicepolicy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class ServicePolicyTest {

    final UUID id = UUID.fromString("0ebfecbc-3907-45c7-b9c6-36d42ec0efa1");
    final UUID serviceTemplateId = UUID.fromString("3a74cadd-d55b-4504-8fdf-56f1b6aae79c");
    final String flavorName = "flavorName";
    final String policy = "policy";
    final Boolean enabled = false;
    private ServicePolicy test;

    @BeforeEach
    void setUp() {
        test = new ServicePolicy();
        test.setServiceTemplateId(serviceTemplateId);
        test.setFlavorName(flavorName);
        test.setEnabled(enabled);
        test.setPolicy(policy);
        test.setId(id);
    }

    @Test
    void testGetters() {
        assertThat(test.getId()).isEqualTo(id);
        assertThat(test.getPolicy()).isEqualTo(policy);
        assertThat(test.getFlavorName()).isEqualTo(flavorName);
        assertThat(test.getServiceTemplateId()).isEqualTo(serviceTemplateId);
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
        ServicePolicy test1 = new ServicePolicy();
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
        ServicePolicy test1 = new ServicePolicy();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result = String.format(
                "ServicePolicy(id=%s, policy=%s, serviceTemplateId=%s, flavorName=%s, enabled=%s, "
                        + "createTime=%s, lastModifiedTime=%s)", id, policy, serviceTemplateId,
                flavorName, enabled, null, null);

        assertThat(test.toString()).isEqualTo(result);
    }
}
