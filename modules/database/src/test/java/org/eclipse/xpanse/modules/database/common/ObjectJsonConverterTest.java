/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.common;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import org.junit.jupiter.api.Test;

class ObjectJsonConverterTest {

    private final ObjectJsonConverter test = new ObjectJsonConverter();

    @Test
    void testConvertToDatabaseColumn() {
        assertNull(test.convertToDatabaseColumn(null));
        assertInstanceOf(String.class, test.convertToDatabaseColumn(new Date()));
        assertThrows(IllegalStateException.class, () ->
            test.convertToDatabaseColumn(new Object())
        );
    }

    @Test
    void testConvertToEntityAttribute() {
        assertNull(test.convertToEntityAttribute(null));
        String s = test.convertToDatabaseColumn(new Date());
        assertInstanceOf(Long.class, test.convertToEntityAttribute(s));
        assertThrows(IllegalStateException.class, () ->
            test.convertToEntityAttribute("errorJson"));
    }
}
