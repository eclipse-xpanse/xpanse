/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployResult.
 */
class DeployResultTest {

    private static final UUID id = UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2");
    private static final String name = "name";
    private static final TerraformExecState state = TerraformExecState.DEPLOY_SUCCESS;
    private static final Map<String, String> map = Map.of("key", "value");
    private static final DeployResource deployResource1 = new DeployResource();
    private static final DeployResource deployResource2 = new DeployResource();
    private static final List<DeployResource> resources = List.of(deployResource1);
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");
    private static final Map<String, String> privateProperties =
            Collections.singletonMap("privateKey", "privateValue");

    @BeforeEach
    void setUp() {
        deployResource1.setResourceId(id.toString());
        deployResource1.setName(name);
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(map);

        deployResource2.setResourceId(
                UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5").toString());
        deployResource2.setName(name);
        deployResource2.setKind(DeployResourceKind.VPC);
        deployResource2.setProperties(map);
    }

    @Test
    void testConstructorAndGetters() {
        DeployResult deployResult = new DeployResult();
        deployResult.setId(id);
        deployResult.setState(state);
        deployResult.setResources(resources);
        deployResult.setProperties(properties);
        deployResult.setPrivateProperties(privateProperties);

        assertEquals(id, deployResult.getId());
        assertEquals(state, deployResult.getState());
        assertEquals(resources, deployResult.getResources());
        assertEquals(properties, deployResult.getProperties());
        assertEquals(privateProperties, deployResult.getPrivateProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        DeployResult deployResult1 = new DeployResult();
        deployResult1.setId(id);
        deployResult1.setState(state);
        deployResult1.setResources(resources);
        deployResult1.setProperties(properties);
        deployResult1.setPrivateProperties(privateProperties);

        DeployResult deployResult2 = new DeployResult();
        deployResult2.setId(id);
        deployResult2.setState(state);
        deployResult2.setResources(resources);
        deployResult2.setProperties(properties);
        deployResult2.setPrivateProperties(privateProperties);

        DeployResult deployResult3 = new DeployResult();
        deployResult3.setId(UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5"));
        deployResult3.setState(TerraformExecState.DEPLOY_FAILED);
        deployResult3.setResources(List.of(deployResource2));
        deployResult3.setProperties(Collections.singletonMap("key2", "value2"));
        deployResult3.setPrivateProperties(
                Collections.singletonMap("privateKey2", "privateValue2"));

        assertEquals(deployResult1, deployResult1);
        assertEquals(deployResult1, deployResult2);
        assertNotEquals(deployResult1, deployResult3);

        assertEquals(deployResult1.hashCode(), deployResult1.hashCode());
        assertEquals(deployResult1.hashCode(), deployResult2.hashCode());
        assertNotEquals(deployResult1.hashCode(), deployResult3.hashCode());
    }

    @Test
    void testToString() {
        DeployResult deployResult = new DeployResult();
        deployResult.setId(id);
        deployResult.setState(state);
        deployResult.setResources(resources);
        deployResult.setProperties(properties);
        deployResult.setPrivateProperties(privateProperties);

        String expectedToString =
                "DeployResult(id=" + id + ", state=" + state + ", resources=" + resources +
                        ", properties=" + properties + ", privateProperties=" + privateProperties +
                        ")";
        assertEquals(expectedToString, deployResult.toString());
    }

}
