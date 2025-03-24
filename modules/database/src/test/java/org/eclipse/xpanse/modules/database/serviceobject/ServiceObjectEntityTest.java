/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceobject;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test for ServiceChangeRequestEntity. */
public class ServiceObjectEntityTest {

    private final UUID objectId = UUID.randomUUID();
    @Mock private ServiceDeploymentEntity serviceDeploymentEntity;
    private final String objectType = "ObjectType";
    private final String objectIdentifierName = "test";
    private final Map<String, Object> properties = Map.of();
    private final Set<UUID> dependentObjectIds = new HashSet<>();

    private ServiceObjectEntity test;

    @BeforeEach
    void setUp() {
        test = new ServiceObjectEntity();
        test.setObjectId(objectId);
        test.setServiceDeploymentEntity(serviceDeploymentEntity);
        test.setObjectType(objectType);
        test.setObjectIdentifierName(objectIdentifierName);
        test.setProperties(properties);
        test.setDependentObjectIds(dependentObjectIds);
    }

    @Test
    void testGetters() {
        assertThat(test.getObjectId()).isEqualTo(objectId);
        assertThat(test.getServiceDeploymentEntity()).isEqualTo(serviceDeploymentEntity);
        assertThat(test.getObjectType()).isEqualTo(objectType);
        assertThat(test.getObjectIdentifierName()).isEqualTo(objectIdentifierName);
        assertThat(test.getProperties()).isEqualTo(properties);
        assertThat(test.getDependentObjectIds()).isEqualTo(dependentObjectIds);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        ServiceObjectEntity test1 = new ServiceObjectEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testHashCode() {
        assertThat(test.hashCode() == new Object().hashCode()).isFalse();
        ServiceObjectEntity test1 = new ServiceObjectEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.hashCode() == test1.hashCode()).isTrue();
    }

    @Test
    void testToString() {
        String result =
                String.format(
                        "ServiceObjectEntity(objectId=%s, "
                                + "serviceDeploymentEntity=%s, "
                                + "objectType=%s, "
                                + "objectIdentifierName=%s, "
                                + "properties=%s, "
                                + "dependentObjectIds=%s)",
                        objectId,
                        serviceDeploymentEntity,
                        objectType,
                        objectIdentifierName,
                        properties,
                        dependentObjectIds);

        assertThat(test.toString()).isEqualTo(result);
    }
}
