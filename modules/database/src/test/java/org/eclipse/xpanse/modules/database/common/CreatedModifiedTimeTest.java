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

class CreatedModifiedTimeTest {

    private static final OffsetDateTime createdTime = OffsetDateTime.now();

    private static final OffsetDateTime lastModifiedTime = OffsetDateTime.now();

    private CreatedModifiedTime test;

    @BeforeEach
    void setUp() {
        test = new CreatedModifiedTime();
        test.setCreatedTime(createdTime);
        test.setLastModifiedTime(lastModifiedTime);
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(test, test);
        assertEquals(test.hashCode(), test.hashCode());

        Object o = new Object();
        assertNotEquals(test, o);
        assertNotEquals(test.hashCode(), o.hashCode());

        CreatedModifiedTime test1 = new CreatedModifiedTime();
        CreatedModifiedTime test2 = new CreatedModifiedTime();
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test.hashCode(), test2.hashCode());
        assertEquals(test1.hashCode(), test2.hashCode());

        test1.setCreatedTime(createdTime);
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
                "CreatedModifiedTime(createdTime="
                        + createdTime
                        + ", "
                        + "lastModifiedTime="
                        + lastModifiedTime
                        + ")";
        assertEquals(expectedToString, test.toString());
    }
}
