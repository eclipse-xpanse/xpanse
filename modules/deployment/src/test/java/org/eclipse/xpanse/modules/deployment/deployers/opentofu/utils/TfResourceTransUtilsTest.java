/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.resources.TfStateResourceInstance;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of TfResourceTransUtils.
 */
class TfResourceTransUtilsTest {

    private static final String id = "ed6248d4-2bcd-4e94-84b0-29e014c05137";
    private static final String name = "Huawei_VM";
    private static final DeployResourceKind kind = DeployResourceKind.VM;
    private static TfStateResourceInstance tfStateResourceInstance;
    private static Map<String, Object> attributes;
    private static DeployResource deployResource;
    private static Map<String, String> keyProperty;

    @BeforeEach
    void setup() {
        attributes = new HashMap<>();
        attributes.put("id", id);
        attributes.put("name", name);
        attributes.put("kind", kind);
        attributes.put("key", "value");

        deployResource = new DeployResource();

        keyProperty = new HashMap<>();
        keyProperty.put("resourceId", "id");
        keyProperty.put("kind", "kind");
        keyProperty.put("customKey", "key");

        tfStateResourceInstance = new TfStateResourceInstance();
        tfStateResourceInstance.setAttributes(attributes);
    }

    @Test
    void testFillDeployResource() {
        TfResourceTransUtils.fillDeployResource(tfStateResourceInstance, deployResource,
                keyProperty);

        // Verify the results
        assertEquals(id, deployResource.getResourceId());
        assertEquals(name, deployResource.getName());
    }

    @Test
    void testFillDeployResource_attributesIsNull() {
        tfStateResourceInstance.setAttributes(null);
        TfResourceTransUtils.fillDeployResource(tfStateResourceInstance, deployResource,
                keyProperty);

        // Verify the results
        assertNull(deployResource.getResourceId());
        assertNull(deployResource.getName());
    }


    @Test
    void testFillDeployResource_keyPropertyIsNull() {
        keyProperty.clear();
        TfResourceTransUtils.fillDeployResource(tfStateResourceInstance, deployResource,
                keyProperty);

        // Verify the results
        assertEquals(id, deployResource.getResourceId());
        assertEquals(name, deployResource.getName());
    }

}
