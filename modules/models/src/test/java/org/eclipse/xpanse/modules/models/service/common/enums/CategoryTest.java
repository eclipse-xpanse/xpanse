/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.common.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of Category.
 */
class CategoryTest {

    @Test
    void testGetByValue() {
        Assertions.assertEquals(Category.AI, Category.getByValue("ai"));
        Assertions.assertEquals(Category.COMPUTE, Category.getByValue("compute"));
        Assertions.assertEquals(Category.CONTAINER, Category.getByValue("container"));
        Assertions.assertEquals(Category.STORAGE, Category.getByValue("storage"));
        Assertions.assertEquals(Category.NETWORK, Category.getByValue("network"));
        Assertions.assertEquals(Category.DATABASE, Category.getByValue("database"));
        Assertions.assertEquals(Category.MEDIA_SERVICE, Category.getByValue("mediaService"));
        Assertions.assertEquals(Category.SECURITY, Category.getByValue("security"));
        Assertions.assertEquals(Category.MIDDLEWARE, Category.getByValue("middleware"));
        Assertions.assertEquals(Category.OTHERS, Category.getByValue("others"));
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> Category.getByValue("null"));
    }

    @Test
    void testToValue() {
        Assertions.assertEquals("ai", Category.AI.toValue());
        Assertions.assertEquals("compute", Category.COMPUTE.toValue());
        Assertions.assertEquals("container", Category.CONTAINER.toValue());
        Assertions.assertEquals("storage", Category.STORAGE.toValue());
        Assertions.assertEquals("network", Category.NETWORK.toValue());
        Assertions.assertEquals("database", Category.DATABASE.toValue());
        Assertions.assertEquals("mediaService", Category.MEDIA_SERVICE.toValue());
        Assertions.assertEquals("security", Category.SECURITY.toValue());
        Assertions.assertEquals("middleware", Category.MIDDLEWARE.toValue());
        Assertions.assertEquals("others", Category.OTHERS.toValue());
    }

}
