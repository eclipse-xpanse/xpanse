/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.junit.jupiter.api.Test;

/**
 * Test of Ocl.
 */
class OclTest {

    private static final String name = "kafka";
    private static final Deployment deployment = new Deployment();
    private static final String version = "2.0";

    @Test
    public void testDeepCopyAnEmptyOcl() {
        Ocl ocl = new Ocl();
        Ocl aCopy = ocl.deepCopy();
        assertNull(aCopy.getName());
    }

    @Test
    public void testDeepCopy() {
        Ocl ocl = new Ocl();
        ocl.setName(name);
        ocl.setVersion(version);
        ocl.setCategory(Category.COMPUTE);

        ocl.setDeployment(deployment);
        deployment.setDeployer("terraform");
        Ocl aCopy = ocl.deepCopy();
        assertEquals(ocl.getName(), aCopy.getName());
        assertNotSame(ocl, aCopy);
        assertNotSame(ocl.getName(), aCopy.getName());
        assertNotSame(ocl.getDeployment(), aCopy.getDeployment());
        Deployment aCopiedDeploy = aCopy.getDeployment();
        assertNotSame(aCopiedDeploy, deployment);
        assertEquals("terraform", aCopiedDeploy.getDeployer());
    }

    @Test
    public void testEqualsAndHashCode() {
        Ocl ocl1 = new Ocl();
        ocl1.setName(name);
        ocl1.setVersion(version);
        ocl1.setCategory(Category.COMPUTE);

        Ocl ocl2 = new Ocl();
        ocl2.setName(name);
        ocl2.setVersion(version);
        ocl2.setCategory(Category.COMPUTE);

        Ocl ocl3 = new Ocl();
        ocl3.setName("zookeeper");
        ocl3.setVersion(version);
        ocl3.setCategory(Category.COMPUTE);

        assertTrue(ocl1.equals(ocl2));
        assertEquals(ocl1.hashCode(), ocl2.hashCode());
        assertFalse(ocl1.equals(ocl3));
        assertNotEquals(ocl1.hashCode(), ocl3.hashCode());
    }
}
