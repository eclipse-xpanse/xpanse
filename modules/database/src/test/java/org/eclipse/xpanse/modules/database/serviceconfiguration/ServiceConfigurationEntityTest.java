/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

/**
 * Test of ServiceConfigurationEntityTest.
 */
@ExtendWith(MockitoExtension.class)
class ServiceConfigurationEntityTest {

    final UUID id = UUID.randomUUID();
    final OffsetDateTime createTime = OffsetDateTime.now();

    private ServiceConfigurationEntity test;

    @Mock
    private ServiceDeploymentEntity serviceDeploymentEntity;

    @BeforeEach
    void setUp() {
        test = new ServiceConfigurationEntity();
        test.setId(id);
        test.setServiceDeploymentEntity(serviceDeploymentEntity);
        test.setConfiguration(getConfiguration());
        test.setCreatedTime(createTime);
    }

    @Test
    void testGetters() {
        assertThat(test.getId()).isEqualTo(id);
        assertThat(test.getConfiguration()).isEqualTo(getConfiguration());
        assertThat(test.getCreatedTime()).isEqualTo(createTime);
        assertThat(test.getServiceDeploymentEntity()).isEqualTo(serviceDeploymentEntity);
    }

    @Test
    void testCreateTimeGetterAndSetter() {
        final OffsetDateTime createTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        test.setCreatedTime(createTime);
        assertThat(test.getCreatedTime()).isEqualTo(createTime);
    }

    @Test
    void testLastModifiedTimeGetterAndSetter() {
        final OffsetDateTime updatedTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        test.setUpdatedTime(updatedTime);
        assertThat(test.getUpdatedTime()).isEqualTo(updatedTime);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        ServiceConfigurationEntity test1 = new ServiceConfigurationEntity();
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
        ServiceConfigurationEntity test1 = new ServiceConfigurationEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result = String.format(
                "ServiceConfigurationEntity(id=%s, serviceDeploymentEntity=%s, configuration=%s, "
                        + "createdTime=%s, "
                        + "updatedTime=%s)",
                id, serviceDeploymentEntity, getConfiguration(), createTime, null);

        assertThat(test.toString()).isEqualTo(result);
    }


    private Map<String, Object> getConfiguration(){
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("key1","value1");
        configuration.put("key2","value2");
        configuration.put("key3","value3");
        return configuration;
    }
}
