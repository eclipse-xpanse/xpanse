/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateModifiedTimeTest {

    private static final OffsetDateTime createTime = OffsetDateTime.now();

    private static final OffsetDateTime lastModifiedTime = OffsetDateTime.now();

    private CreateModifiedTime test;

    @BeforeEach
    void setUp() {
        test = new CreateModifiedTime();
        test.setCreateTime(createTime);
        test.setLastModifiedTime(lastModifiedTime);
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(test, test);
        assertEquals(test.hashCode(), test.hashCode());

        Object o = new Object();
        assertNotEquals(test, o);
        assertNotEquals(test.hashCode(), o.hashCode());

        CreateModifiedTime test1 = new CreateModifiedTime();
        CreateModifiedTime test2 = new CreateModifiedTime();
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test.hashCode(), test2.hashCode());
        assertEquals(test1.hashCode(), test2.hashCode());

        test1.setCreateTime(createTime);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setLastModifiedTime(lastModifiedTime);
        assertEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "CreateModifiedTime(createTime="
                        + createTime
                        + ", "
                        + "lastModifiedTime="
                        + lastModifiedTime
                        + ")";
        assertEquals(expectedToString, test.toString());
    }
}
