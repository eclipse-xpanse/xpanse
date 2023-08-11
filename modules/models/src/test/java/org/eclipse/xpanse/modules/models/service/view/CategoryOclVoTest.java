/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.validation.Valid;
import java.util.List;
import org.eclipse.xpanse.modules.models.servicetemplate.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.VersionOclVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of CategoryOclVo.
 */
class CategoryOclVoTest {

    private static final String name = "compute";
    private List<@Valid VersionOclVo> versions;
    private CategoryOclVo categoryOclVo;


    @BeforeEach
    void setUp() {
        VersionOclVo versionOclVo = new VersionOclVo();
        versionOclVo.setVersion("v1.0.0");
        versions = List.of(versionOclVo);

        categoryOclVo = new CategoryOclVo();
        categoryOclVo.setName(name);
        categoryOclVo.setVersions(versions);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, categoryOclVo.getName());
        assertEquals(versions, categoryOclVo.getVersions());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(categoryOclVo, categoryOclVo);
        assertEquals(categoryOclVo.hashCode(), categoryOclVo.hashCode());

        Object obj = new Object();
        assertNotEquals(categoryOclVo, obj);
        assertNotEquals(categoryOclVo, null);
        assertNotEquals(categoryOclVo.hashCode(), obj.hashCode());

        CategoryOclVo categoryOclVo1 = new CategoryOclVo();
        CategoryOclVo categoryOclVo2 = new CategoryOclVo();
        assertNotEquals(categoryOclVo, categoryOclVo1);
        assertNotEquals(categoryOclVo, categoryOclVo2);
        assertEquals(categoryOclVo1, categoryOclVo2);
        assertNotEquals(categoryOclVo.hashCode(), categoryOclVo1.hashCode());
        assertNotEquals(categoryOclVo.hashCode(), categoryOclVo2.hashCode());
        assertEquals(categoryOclVo1.hashCode(), categoryOclVo2.hashCode());

        categoryOclVo1.setName(name);
        assertNotEquals(categoryOclVo, categoryOclVo1);
        assertNotEquals(categoryOclVo1, categoryOclVo2);
        assertNotEquals(categoryOclVo.hashCode(), categoryOclVo1.hashCode());
        assertNotEquals(categoryOclVo1.hashCode(), categoryOclVo2.hashCode());

        categoryOclVo1.setVersions(versions);
        assertEquals(categoryOclVo, categoryOclVo1);
        assertNotEquals(categoryOclVo1, categoryOclVo2);
        assertEquals(categoryOclVo.hashCode(), categoryOclVo1.hashCode());
        assertNotEquals(categoryOclVo1.hashCode(), categoryOclVo2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "CategoryOclVo(" +
                "name=" + name + ", " +
                "versions=" + versions + ")";
        assertEquals(expectedToString, categoryOclVo.toString());
    }

}
