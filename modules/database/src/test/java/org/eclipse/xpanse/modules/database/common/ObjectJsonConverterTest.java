/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.common;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObjectJsonConverterTest {

    private ObjectJsonConverter test;

    @BeforeEach
    void setUp() {
        test = new ObjectJsonConverter();
    }

    @Test
    void testConvertToDatabaseColumn() {
        assertInstanceOf(String.class, test.convertToDatabaseColumn(new Date()));
        assertThrows(IllegalStateException.class, () -> {
            test.convertToDatabaseColumn(new Object());
        });
    }

    @Test
    void testConvertToEntityAttribute() {

        String s = test.convertToDatabaseColumn(new Date());
        assertInstanceOf(Long.class, test.convertToEntityAttribute(s));
        assertThrows(IllegalStateException.class, () -> {
            test.convertToEntityAttribute(StringUtils.EMPTY);
        });
    }
}
