/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test of DeployedServiceDetails. */
class DeployedServiceDetailsTest {

    @Mock private List<@Valid DeployResource> deployResources;
    private Map<String, String> deployedServiceProperties;

    private DeployedServiceDetails test;

    @BeforeEach
    void setUp() {
        deployedServiceProperties = Map.of("key", "value");
        test = new DeployedServiceDetails();
        test.setDeployResources(deployResources);
        test.setDeployedServiceProperties(deployedServiceProperties);
    }

    @Test
    void testGetterAndSetter() {
        assertThat(deployResources).isEqualTo(test.getDeployResources());
        assertThat(deployedServiceProperties).isEqualTo(test.getDeployedServiceProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        Object object = new Object();
        assertThat(test).isNotEqualTo(object);
        assertThat(test.hashCode()).isNotEqualTo(object.hashCode());

        DeployedServiceDetails test1 = new DeployedServiceDetails();
        assertThat(test.equals(test1)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "DeployedServiceDetails(deployResources="
                        + deployResources
                        + ", deployedServiceProperties="
                        + deployedServiceProperties
                        + ")";

        assertEquals(expectedToString, test.toString());
    }
}
