/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader;

import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Storage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        ocl.setStorage(Arrays.asList(storage));
        Ocl aCopy = ocl.deepCopy();
        assertEquals("foo", aCopy.getName());
        assertNotSame(ocl, aCopy);
        assertNotSame(ocl.getName(), aCopy.getName());
        assertNotSame(ocl.getStorage(), aCopy.getStorage());
        Storage aCopiedStorage = aCopy.getStorage().get(0);
        assertNotSame(aCopiedStorage, storage);
        assertEquals("bar", aCopiedStorage.getName());
    }
}
