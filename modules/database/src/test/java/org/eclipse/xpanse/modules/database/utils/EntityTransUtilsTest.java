/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntityTransUtilsTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String RESOURCE_ID = "resourceId";
    private static final String NAME = "name";
    private static final DeployResourceKind KIND = DeployResourceKind.VM;
    private DeployServiceEntity deployService = new DeployServiceEntity();
    private Map<String, String> properties = new HashMap<>();


    private DeployResource deployResource = new DeployResource();
    private DeployResourceEntity deployResourceEntity = new DeployResourceEntity();


    @BeforeEach
    void setUp() {
        deployResourceEntity.setId(ID);
        deployResourceEntity.setResourceId(RESOURCE_ID);
        deployResourceEntity.setName(NAME);
        deployResourceEntity.setKind(KIND);

        deployResource.setResourceId(RESOURCE_ID);
        deployResource.setName(NAME);
        deployResource.setKind(KIND);
    }

    @Test
    void testTransResourceEntity() {
        deployResourceEntity.setProperties(Map.ofEntries(Map.entry("ip", "ip")));
        deployResource.setProperties(Map.ofEntries(Map.entry("ip", "ip")));
        List<DeployResourceEntity> entities = List.of(deployResourceEntity);
        final List<DeployResource> expectedResult = List.of(deployResource);
        final List<DeployResource> result = EntityTransUtils.transResourceEntity(entities);
        assertThat(result).isNotEqualTo(expectedResult);
    }

    @Test
    void testTransResourceEntity_emptyProperties() {
        deployResourceEntity.setProperties(properties);
        deployResource.setProperties(properties);
        List<DeployResourceEntity> entities = List.of(deployResourceEntity);
        final List<DeployResource> expectedResult = List.of(deployResource);
        final List<DeployResource> result = EntityTransUtils.transResourceEntity(entities);
        assertThat(result).isNotEqualTo(expectedResult);
    }

    @Test
    void testTransResourceEntity_emptyDeployResourceEntityList() {
        List<DeployResourceEntity> entities = new ArrayList<>();
        final List<DeployResource> result = EntityTransUtils.transResourceEntity(entities);
        assertEquals(0, result.size());
    }

    @Test
    void test_Constructor() {
        EntityTransUtils utils = new EntityTransUtils();
        EntityTransUtils utils1 = new EntityTransUtils();
        assertNotEquals(utils, utils1);
        assertNotEquals(utils, utils1);
    }

}
