/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.system.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of DatabaseType.
 */
class DatabaseTypeTest {

    @Test
    public void testGetByValue() {
        DatabaseType type = DatabaseType.getByValue("h2");
        Assertions.assertEquals(type, DatabaseType.H2DB);

        DatabaseType type1 = DatabaseType.getByValue("mysql");
        Assertions.assertEquals(type1, DatabaseType.MYSQL);

        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> DatabaseType.getByValue(null));
    }

    @Test
    public void testToValue() {
        String value = DatabaseType.H2DB.toValue();
        Assertions.assertEquals("h2", value);

        String name = DatabaseType.H2DB.name();
        Assertions.assertEquals("H2DB", name);

        String string = DatabaseType.H2DB.toString();
        Assertions.assertEquals("H2DB", string);

        String value1 = DatabaseType.MYSQL.toValue();
        Assertions.assertEquals("mysql", value1);

        String name1 = DatabaseType.MYSQL.name();
        Assertions.assertEquals("MYSQL", name1);

        String string1 = DatabaseType.MYSQL.toString();
        Assertions.assertEquals("MYSQL", string1);
    }

    @Test
    public void testEqualsAndHashCode() {
        Assertions.assertEquals(DatabaseType.H2DB.hashCode(), DatabaseType.H2DB.hashCode());
        Assertions.assertEquals(DatabaseType.MYSQL.hashCode(), DatabaseType.MYSQL.hashCode());
        Assertions.assertNotEquals(DatabaseType.H2DB.hashCode(), DatabaseType.MYSQL.hashCode());
    }

}
