/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Storage;
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
        Storage storage = new Storage();
        storage.setName("bar");
        ocl.setStorages(Arrays.asList(storage));
        Ocl aCopy = ocl.deepCopy();
        assertEquals("foo", aCopy.getName());
        assertNotSame(ocl, aCopy);
        assertNotSame(ocl.getName(), aCopy.getName());
        assertNotSame(ocl.getStorages(), aCopy.getStorages());
        Storage aCopiedStorage = aCopy.getStorages().get(0);
        assertNotSame(aCopiedStorage, storage);
        assertEquals("bar", aCopiedStorage.getName());
    }
}
