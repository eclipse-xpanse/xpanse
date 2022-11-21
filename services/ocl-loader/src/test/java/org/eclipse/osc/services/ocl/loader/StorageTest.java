package org.eclipse.osc.services.ocl.loader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageTest {

    @Test
    public void testEquals() {
        Storage storage = new Storage();
        Storage anotherStorage = new Storage();
        storage.setState("some state");
        anotherStorage.setState("another state");
        storage.setName("sameName");
        anotherStorage.setName("sameName");
        Assertions.assertNotEquals(storage, anotherStorage);
    }

}
