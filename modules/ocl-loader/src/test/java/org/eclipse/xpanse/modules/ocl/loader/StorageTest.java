/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader;

import org.eclipse.xpanse.modules.ocl.loader.data.models.Storage;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.RuntimeState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageTest {

    @Test
    public void testEquals() {
        Storage storage = new Storage();
        Storage anotherStorage = new Storage();
        storage.setState(RuntimeState.ACTIVE);
        anotherStorage.setState(RuntimeState.INACTIVE);
        storage.setName("sameName");
        anotherStorage.setName("sameName");
        Assertions.assertNotEquals(storage, anotherStorage);
        Assertions.assertNotEquals(storage.hashCode(), anotherStorage.hashCode());
    }

}
