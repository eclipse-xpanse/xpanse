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
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.serviceconfiguration.AnsibleTaskResult;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test for ServiceChangeDetailsEntity. */
public class ServiceChangeDetailsEntityTest {

    final UUID id = UUID.randomUUID();
    final String resourceName = "zookeeper-1234";
    final String changeHandler = "zookeeper";
    final String resultMessage = "resultMessage";
    final Map<String, Object> properties = Map.of("k1", "v1", "k2", "v2");
    final ServiceConfigurationStatus status = ServiceConfigurationStatus.PENDING;
    final List<AnsibleTaskResult> tasks = List.of();
    @Mock private ServiceDeploymentEntity serviceDeploymentEntity;
    @Mock private ServiceOrderEntity serviceOrderEntity;

    private ServiceChangeDetailsEntity test;

    @BeforeEach
    void setUp() {
        test = new ServiceChangeDetailsEntity();
        test.setId(id);
        test.setServiceDeploymentEntity(serviceDeploymentEntity);
        test.setServiceOrderEntity(serviceOrderEntity);
        test.setResourceName(resourceName);
        test.setChangeHandler(changeHandler);
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
        assertThat(test.getChangeHandler()).isEqualTo(changeHandler);
        assertThat(test.getResultMessage()).isEqualTo(resultMessage);
        assertThat(test.getStatus()).isEqualTo(status);
        assertThat(test.getProperties()).isEqualTo(properties);
        assertThat(test.getTasks()).isEqualTo(tasks);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        ServiceChangeDetailsEntity test1 = new ServiceChangeDetailsEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testHashCode() {
        assertThat(test.hashCode() == new Object().hashCode()).isFalse();
        ServiceChangeDetailsEntity test1 = new ServiceChangeDetailsEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result =
                String.format(
                        "ServiceChangeDetailsEntity(id=%s, "
                                + "serviceOrderEntity=%s, "
                                + "serviceDeploymentEntity=%s, "
                                + "resourceName=%s, "
                                + "changeHandler=%s, "
                                + "resultMessage=%s, "
                                + "properties=%s, "
                                + "status=%s, "
                                + "tasks=%s)",
                        id,
                        serviceOrderEntity,
                        serviceDeploymentEntity,
                        resourceName,
                        changeHandler,
                        resultMessage,
                        properties,
                        status,
                        tasks);

        assertThat(test.toString()).isEqualTo(result);
    }
}
