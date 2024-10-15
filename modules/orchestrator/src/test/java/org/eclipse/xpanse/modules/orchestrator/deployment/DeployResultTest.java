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
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/**
 * Test of DeployResult.
 */
class DeployResultTest {

    private final UUID orderId = UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2");
    private final UUID serviceId = UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2");
    private final boolean isSuccessful = true;
    private final Map<String, String> properties = Collections.singletonMap("key", "value");
    private final Map<String, String> privateProperties =
            Collections.singletonMap("privateKey", "privateValue");

    private final String message = "success";

    private final DeployerTaskStatus state = DeployerTaskStatus.DEPLOY_SUCCESS;

    private final String tfStateContent = "tfStateContent";

    private final String deployerVersionUsed = "1.6.0";

    @Mock
    private List<DeployResource> resources;
    private DeployResult test;

    @BeforeEach
    void setUp() {
        test = new DeployResult();
        test.setOrderId(orderId);
        test.setServiceId(serviceId);
        test.setIsTaskSuccessful(isSuccessful);
        test.setResources(resources);
        test.setProperties(properties);
        test.setPrivateProperties(privateProperties);
        test.setMessage(message);
        test.setState(state);
        test.setTfStateContent(tfStateContent);
        test.setDeployerVersionUsed(deployerVersionUsed);
    }

    @Test
    void testGetters() {
        assertEquals(orderId, test.getOrderId());
        assertEquals(serviceId, test.getServiceId());
        assertEquals(isSuccessful, test.getIsTaskSuccessful());
        assertEquals(message, test.getMessage());
        assertEquals(resources, test.getResources());
        assertEquals(properties, test.getProperties());
        assertEquals(privateProperties, test.getPrivateProperties());
        assertEquals(state, test.getState());
        assertEquals(tfStateContent, test.getTfStateContent());
        assertEquals(deployerVersionUsed, test.getDeployerVersionUsed());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertNotEquals(test, obj);
        assertNotEquals(test.hashCode(), obj.hashCode());

        DeployResult test1 = new DeployResult();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertEquals(test, test1);
        assertEquals(test.hashCode(), test1.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "DeployResult(orderId=" + orderId
                + ", serviceId=" + serviceId
                + ", isTaskSuccessful=" + isSuccessful
                + ", state=" + state
                + ", message=" + message
                + ", resources=" + resources
                + ", properties=" + properties
                + ", privateProperties=" + privateProperties
                + ", deployerVersionUsed=" + deployerVersionUsed
                + ", tfStateContent=" + tfStateContent
                + ")";
        assertEquals(expectedToString, test.toString());
    }

}
