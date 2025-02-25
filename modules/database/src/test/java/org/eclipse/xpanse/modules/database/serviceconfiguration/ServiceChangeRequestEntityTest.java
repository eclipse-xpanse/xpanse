/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.servicechange.AnsibleTaskResult;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test for ServiceChangeRequestEntity. */
public class ServiceChangeRequestEntityTest {

    final UUID id = UUID.randomUUID();
    final String resourceName = "zookeeper-1234";
    final String changeHandler = "zookeeper";
    final String resultMessage = "resultMessage";
    final Map<String, Object> properties = Map.of("k1", "v1", "k2", "v2");
    final ServiceChangeStatus status = ServiceChangeStatus.PENDING;
    final List<AnsibleTaskResult> tasks = List.of();
    final OffsetDateTime createdTime = OffsetDateTime.now();
    final OffsetDateTime lastModifiedTime = OffsetDateTime.now();
    @Mock private ServiceDeploymentEntity serviceDeploymentEntity;
    @Mock private ServiceOrderEntity serviceOrderEntity;

    private ServiceChangeRequestEntity test;

    @BeforeEach
    void setUp() {
        test = new ServiceChangeRequestEntity();
        test.setId(id);
        test.setServiceDeploymentEntity(serviceDeploymentEntity);
        test.setServiceOrderEntity(serviceOrderEntity);
        test.setResourceName(resourceName);
        test.setChangeHandler(changeHandler);
        test.setResultMessage(resultMessage);
        test.setStatus(status);
        test.setProperties(properties);
        test.setTasks(tasks);
        test.setCreatedTime(createdTime);
        test.setLastModifiedTime(lastModifiedTime);
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
        assertThat(test.getCreatedTime()).isEqualTo(createdTime);
        assertThat(test.getLastModifiedTime()).isEqualTo(lastModifiedTime);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        ServiceChangeRequestEntity test1 = new ServiceChangeRequestEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testHashCode() {
        assertThat(test.hashCode() == new Object().hashCode()).isFalse();
        ServiceChangeRequestEntity test1 = new ServiceChangeRequestEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result =
                String.format(
                        "ServiceChangeRequestEntity(id=%s, "
                                + "serviceOrderEntity=%s, "
                                + "serviceDeploymentEntity=%s, "
                                + "resourceName=%s, "
                                + "changeHandler=%s, "
                                + "resultMessage=%s, "
                                + "properties=%s, "
                                + "status=%s, "
                                + "tasks=%s, "
                                + "originalRequestProperties=null)",
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
