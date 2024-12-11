/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.serviceconfiguration.AnsibleTaskResult;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test for ServiceConfigurationChangeDetailsEntity. */
public class ServiceConfigurationChangeDetailsEntityTest {

    final UUID id = UUID.randomUUID();
    final String resourceName = "zookeeper-1234";
    final String configManager = "zookeeper";
    final String resultMessage = "resultMessage";
    final Map<String, Object> properties = Map.of("k1", "v1", "k2", "v2");
    final ServiceConfigurationStatus status = ServiceConfigurationStatus.PENDING;
    final List<AnsibleTaskResult> tasks = List.of();
    @Mock private ServiceDeploymentEntity serviceDeploymentEntity;
    @Mock private ServiceOrderEntity serviceOrderEntity;

    private ServiceConfigurationChangeDetailsEntity test;

    @BeforeEach
    void setUp() {
        test = new ServiceConfigurationChangeDetailsEntity();
        test.setId(id);
        test.setServiceDeploymentEntity(serviceDeploymentEntity);
        test.setServiceOrderEntity(serviceOrderEntity);
        test.setResourceName(resourceName);
        test.setConfigManager(configManager);
        test.setResultMessage(resultMessage);
        test.setStatus(status);
        test.setProperties(properties);
        test.setTasks(tasks);
    }

    @Test
    void testGetters() {
        assertThat(test.getId()).isEqualTo(id);
        assertThat(test.getServiceDeploymentEntity()).isEqualTo(serviceDeploymentEntity);
        assertThat(test.getServiceOrderEntity()).isEqualTo(serviceOrderEntity);
        assertThat(test.getResourceName()).isEqualTo(resourceName);
        assertThat(test.getConfigManager()).isEqualTo(configManager);
        assertThat(test.getResultMessage()).isEqualTo(resultMessage);
        assertThat(test.getStatus()).isEqualTo(status);
        assertThat(test.getProperties()).isEqualTo(properties);
        assertThat(test.getTasks()).isEqualTo(tasks);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        ServiceConfigurationChangeDetailsEntity test1 =
                new ServiceConfigurationChangeDetailsEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testHashCode() {
        assertThat(test.hashCode() == new Object().hashCode()).isFalse();
        ServiceConfigurationChangeDetailsEntity test1 =
                new ServiceConfigurationChangeDetailsEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result =
                String.format(
                        "ServiceConfigurationChangeDetailsEntity(id=%s, "
                                + "serviceOrderEntity=%s, "
                                + "serviceDeploymentEntity=%s, "
                                + "resourceName=%s, "
                                + "configManager=%s, "
                                + "resultMessage=%s, "
                                + "properties=%s, "
                                + "status=%s, "
                                + "tasks=%s)",
                        id,
                        serviceOrderEntity,
                        serviceDeploymentEntity,
                        resourceName,
                        configManager,
                        resultMessage,
                        properties,
                        status,
                        tasks);

        assertThat(test.toString()).isEqualTo(result);
    }
}
