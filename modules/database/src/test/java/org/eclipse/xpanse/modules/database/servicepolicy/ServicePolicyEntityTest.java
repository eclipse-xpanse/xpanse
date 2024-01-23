package org.eclipse.xpanse.modules.database.servicepolicy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServicePolicyEntityTest {

    final UUID id = UUID.fromString("0ebfecbc-3907-45c7-b9c6-36d42ec0efa1");
    final String flavorName = "flavorName";
    final String policy = "policy";
    final Boolean enabled = false;
    @Mock
    private ServiceTemplateEntity mockServiceTemplate;
    private ServicePolicyEntity test;

    @BeforeEach
    void setUp() {
        test = new ServicePolicyEntity();
        test.setServiceTemplate(mockServiceTemplate);
        test.setFlavorName(flavorName);
        test.setEnabled(enabled);
        test.setPolicy(policy);
        test.setId(id);
    }

    @Test
    void testGetters() {
        assertThat(test.getId()).isEqualTo(id);
        assertThat(test.getPolicy()).isEqualTo(policy);
        assertThat(test.getServiceTemplate()).isEqualTo(mockServiceTemplate);
        assertThat(test.getFlavorName()).isEqualTo(flavorName);
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
        ServicePolicyEntity test1 = new ServicePolicyEntity();
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
        ServicePolicyEntity test1 = new ServicePolicyEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result = String.format(
                "ServicePolicyEntity(id=%s, policy=%s, serviceTemplate=%s, flavorName=%s, enabled=%s)",
                id, policy, mockServiceTemplate, flavorName, enabled);

        assertThat(test.toString()).isEqualTo(result);
    }
}
