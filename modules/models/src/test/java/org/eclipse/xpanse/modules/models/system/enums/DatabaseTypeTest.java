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

        DatabaseType type1 = DatabaseType.getByValue("mariadb");
        Assertions.assertEquals(type1, DatabaseType.MARIADB);

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

        String value1 = DatabaseType.MARIADB.toValue();
        Assertions.assertEquals("mariadb", value1);

        String name1 = DatabaseType.MARIADB.name();
        Assertions.assertEquals("MARIADB", name1);

        String string1 = DatabaseType.MARIADB.toString();
        Assertions.assertEquals("MARIADB", string1);
    }

    @Test
    public void testEqualsAndHashCode() {
        Assertions.assertEquals(DatabaseType.H2DB.hashCode(), DatabaseType.H2DB.hashCode());
        Assertions.assertEquals(DatabaseType.MARIADB.hashCode(), DatabaseType.MARIADB.hashCode());
        Assertions.assertNotEquals(DatabaseType.H2DB.hashCode(), DatabaseType.MARIADB.hashCode());
    }

}
