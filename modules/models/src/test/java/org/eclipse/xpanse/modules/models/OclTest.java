/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.xpanse.modules.models.resource.Deployment;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.junit.jupiter.api.Test;

public class OclTest {

    @Test
    public void testDeepCopyAnEmptyOcl() throws Exception {
        Ocl ocl = new Ocl();
        Ocl aCopy = ocl.deepCopy();
        assertNull(aCopy.getName());
    }

    @Test
    public void testDeepCopy() throws Exception {
        Ocl ocl = new Ocl();
        ocl.setName("foo");
        Deployment deployment = new Deployment();
        deployment.setDeployer("terraform");
        ocl.setDeployment(deployment);
        Ocl aCopy = ocl.deepCopy();
        assertEquals("foo", aCopy.getName());
        assertNotSame(ocl, aCopy);
        assertNotSame(ocl.getName(), aCopy.getName());
        assertNotSame(ocl.getDeployment(), aCopy.getDeployment());
        Deployment aCopiedDeploy = aCopy.getDeployment();
        assertNotSame(aCopiedDeploy, deployment);
        assertEquals("terraform", aCopiedDeploy.getDeployer());
    }
}
