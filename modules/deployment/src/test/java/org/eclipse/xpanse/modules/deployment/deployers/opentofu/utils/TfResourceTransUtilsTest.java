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
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of TfResourceTransUtils. */
class TfResourceTransUtilsTest {

    private final String id = "ed6248d4-2bcd-4e94-84b0-29e014c05137";
    private final String resourceName = "Huawei_VM";
    private final DeployResourceKind kind = DeployResourceKind.VM;
    private TfStateResourceInstance tfStateResourceInstance;
    private DeployResource deployResource;
    private Map<String, String> keyProperty;
    private Map<String, Object> attributes;

    @BeforeEach
    void setup() {
        attributes = new HashMap<>();
        attributes.put("id", id);
        attributes.put("name", resourceName);
        attributes.put("kind", kind);
        attributes.put("key", "value");

        deployResource = new DeployResource();
        String groupName = "kafka";
        deployResource.setGroupName(groupName);
        String groupType = "huaweicloud_compute_instance";
        deployResource.setGroupType(groupType);
        deployResource.setResourceKind(kind);

        keyProperty = new HashMap<>();
        keyProperty.put("resourceId", "id");
        keyProperty.put("kind", "kind");
        keyProperty.put("customKey", "key");

        tfStateResourceInstance = new TfStateResourceInstance();
        tfStateResourceInstance.setAttributes(attributes);
    }

    @Test
    void testFillDeployResource() {
        TfResourceTransUtils.fillDeployResource(
                tfStateResourceInstance, deployResource, keyProperty);
        // Verify the results
        assertEquals(id, deployResource.getResourceId());
        assertEquals(resourceName, deployResource.getResourceName());

        // attributes without name;
        attributes.remove("name");
        TfResourceTransUtils.fillDeployResource(
                tfStateResourceInstance, deployResource, keyProperty);
        // Verify the results
        assertEquals(id, deployResource.getResourceId());
        assertEquals(id, deployResource.getResourceName());
    }

    @Test
    void testFillDeployResource_attributesIsNull() {
        tfStateResourceInstance.setAttributes(null);
        TfResourceTransUtils.fillDeployResource(
                tfStateResourceInstance, deployResource, keyProperty);
        // Verify the results
        assertNull(deployResource.getResourceId());
        assertNull(deployResource.getResourceName());
    }

    @Test
    void testFillDeployResource_keyPropertyIsNull() {
        keyProperty.clear();
        TfResourceTransUtils.fillDeployResource(
                tfStateResourceInstance, deployResource, keyProperty);
        // Verify the results
        assertEquals(id, deployResource.getResourceId());
        assertEquals(resourceName, deployResource.getResourceName());
    }
}
