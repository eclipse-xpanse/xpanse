/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployResult.
 */
class DeployResultTest {

    private static final UUID id = UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2");
    private static final String name = "name";
    private static final DeployerTaskStatus state = DeployerTaskStatus.DEPLOY_SUCCESS;
    private static final Map<String, String> map = Map.of("key", "value");
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");
    private static final Map<String, String> privateProperties =
            Collections.singletonMap("privateKey", "privateValue");
    private static List<DeployResource> resources;
    private static DeployResult deployResult;

    @BeforeEach
    void setUp() {
        DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId(id.toString());
        deployResource1.setName(name);
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(map);

        DeployResource deployResource2 = new DeployResource();
        deployResource2.setResourceId(
                UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5").toString());
        deployResource2.setName(name);
        deployResource2.setKind(DeployResourceKind.VPC);
        deployResource2.setProperties(map);

        resources = List.of(deployResource1, deployResource2);

        deployResult = new DeployResult();
        deployResult.setId(id);
        deployResult.setState(state);
        deployResult.setResources(resources);
        deployResult.setProperties(properties);
        deployResult.setPrivateProperties(privateProperties);
        deployResult.setMessage(state.toValue());
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(id, deployResult.getId());
        assertEquals(state, deployResult.getState());
        assertEquals(resources, deployResult.getResources());
        assertEquals(properties, deployResult.getProperties());
        assertEquals(privateProperties, deployResult.getPrivateProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertNotEquals(deployResult, obj);
        assertNotEquals(deployResult, null);
        assertNotEquals(deployResult.hashCode(), obj.hashCode());

        DeployResult deployResult1 = new DeployResult();
        DeployResult deployResult2 = new DeployResult();
        assertNotEquals(deployResult, deployResult1);
        assertNotEquals(deployResult, deployResult2);
        assertEquals(deployResult1, deployResult2);
        assertNotEquals(deployResult.hashCode(), deployResult1.hashCode());
        assertNotEquals(deployResult.hashCode(), deployResult2.hashCode());
        assertEquals(deployResult1.hashCode(), deployResult2.hashCode());

        deployResult1.setId(id);
        assertNotEquals(deployResult, deployResult1);
        assertNotEquals(deployResult1, deployResult2);
        assertNotEquals(deployResult.hashCode(), deployResult1.hashCode());
        assertNotEquals(deployResult1.hashCode(), deployResult2.hashCode());

        deployResult1.setState(state);
        assertNotEquals(deployResult, deployResult1);
        assertNotEquals(deployResult1, deployResult2);
        assertNotEquals(deployResult.hashCode(), deployResult1.hashCode());
        assertNotEquals(deployResult1.hashCode(), deployResult2.hashCode());

        deployResult1.setResources(resources);
        assertNotEquals(deployResult, deployResult1);
        assertNotEquals(deployResult1, deployResult2);
        assertNotEquals(deployResult.hashCode(), deployResult1.hashCode());
        assertNotEquals(deployResult1.hashCode(), deployResult2.hashCode());

        deployResult1.setProperties(properties);
        assertNotEquals(deployResult, deployResult1);
        assertNotEquals(deployResult1, deployResult2);
        assertNotEquals(deployResult.hashCode(), deployResult1.hashCode());
        assertNotEquals(deployResult1.hashCode(), deployResult2.hashCode());

        deployResult1.setPrivateProperties(privateProperties);
        assertNotEquals(deployResult, deployResult1);
        assertNotEquals(deployResult1, deployResult2);
        assertNotEquals(deployResult.hashCode(), deployResult1.hashCode());
        assertNotEquals(deployResult1.hashCode(), deployResult2.hashCode());

        deployResult1.setMessage(state.toValue());
        assertEquals(deployResult, deployResult1);
        assertNotEquals(deployResult1, deployResult2);
        assertEquals(deployResult.hashCode(), deployResult1.hashCode());
        assertNotEquals(deployResult1.hashCode(), deployResult2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "DeployResult(id=" + id + ", state=" + state + ", message=" + state.toValue()
                        + ", resources=" + resources + ", properties=" + properties
                        + ", privateProperties=" + privateProperties +
                        ")";
        assertEquals(expectedToString, deployResult.toString());
    }

}
