/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of JsonObjectSchema.
 */
public class JsonObjectSchemaTest {

    private static  Map<String, Map<String, Object>> properties;
    private static List<String> required;
    private static JsonObjectSchema jsonObjectSchema;

    @BeforeEach
    void setUp() {
        Map<String,Object> validationProperties = new HashMap<>();
        validationProperties.put("minLength",8);
        properties = new HashMap<>();
        properties.put("admin_passwd",validationProperties);

        required = new ArrayList<>();
        required.add("admin_passwd");

        jsonObjectSchema = new JsonObjectSchema();
        jsonObjectSchema.setProperties(properties);
        jsonObjectSchema.setRequired(required);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(properties, jsonObjectSchema.getProperties());
        assertEquals(required, jsonObjectSchema.getRequired());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(jsonObjectSchema, jsonObjectSchema);
        assertEquals(jsonObjectSchema.hashCode(), jsonObjectSchema.hashCode());

        Object obj = new Object();
        assertNotEquals(jsonObjectSchema, obj);
        assertNotEquals(jsonObjectSchema, null);
        assertNotEquals(jsonObjectSchema.hashCode(), obj.hashCode());

        JsonObjectSchema jsonObjectSchema1 = new JsonObjectSchema();
        JsonObjectSchema jsonObjectSchema2 = new JsonObjectSchema();
        assertNotEquals(jsonObjectSchema, jsonObjectSchema1);
        assertNotEquals(jsonObjectSchema, jsonObjectSchema2);
        assertEquals(jsonObjectSchema1, jsonObjectSchema2);
        assertNotEquals(jsonObjectSchema.hashCode(), jsonObjectSchema1.hashCode());
        assertNotEquals(jsonObjectSchema.hashCode(), jsonObjectSchema2.hashCode());
        assertEquals(jsonObjectSchema1.hashCode(), jsonObjectSchema2.hashCode());

        jsonObjectSchema1.setProperties(properties);
        assertNotEquals(jsonObjectSchema, jsonObjectSchema1);
        assertNotEquals(jsonObjectSchema1, jsonObjectSchema2);
        assertNotEquals(jsonObjectSchema.hashCode(), jsonObjectSchema1.hashCode());
        assertNotEquals(jsonObjectSchema1.hashCode(), jsonObjectSchema2.hashCode());

        jsonObjectSchema1.setRequired(required);
        assertEquals(jsonObjectSchema, jsonObjectSchema1);
        assertNotEquals(jsonObjectSchema1, jsonObjectSchema2);
        assertEquals(jsonObjectSchema.hashCode(), jsonObjectSchema1.hashCode());
        assertNotEquals(jsonObjectSchema1.hashCode(), jsonObjectSchema2.hashCode());


    }

    @Test
    void testToString() {
        String expectedString = "JsonObjectSchema(" +
                "type=object, " +
                "properties=" + properties +
                ", required=" + required +
                ", additionalProperties=" + false +
                ")";

        assertEquals(expectedString, jsonObjectSchema.toString());
    }
}
