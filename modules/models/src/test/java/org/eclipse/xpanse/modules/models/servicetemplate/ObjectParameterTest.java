/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/** Test of ObjectParameter. */
public class ObjectParameterTest {

    private final String name = "test";
    private final VariableDataType dataType = VariableDataType.STRING;
    private final String description = "description";
    private final Object example = "example";
    private final Map<String, Object> valueSchema = Map.of();
    private final SensitiveScope sensitiveScope = SensitiveScope.ALWAYS;
    private final Boolean isMandatory = Boolean.TRUE;
    private final String linkedObjectType = "database";

    private ObjectParameter objectParameter;

    @BeforeEach
    void setUp() {
        objectParameter = new ObjectParameter();
        objectParameter.setName(name);
        objectParameter.setDataType(dataType);
        objectParameter.setDescription(description);
        objectParameter.setExample(example);
        objectParameter.setValueSchema(valueSchema);
        objectParameter.setSensitiveScope(sensitiveScope);
        objectParameter.setIsMandatory(isMandatory);
        objectParameter.setLinkedObjectType(linkedObjectType);
    }

    @Test
    void testGetters() {
        assertEquals(name, objectParameter.getName());
        assertEquals(dataType, objectParameter.getDataType());
        assertEquals(description, objectParameter.getDescription());
        assertEquals(example, objectParameter.getExample());
        assertEquals(valueSchema, objectParameter.getValueSchema());
        assertEquals(sensitiveScope, objectParameter.getSensitiveScope());
        assertEquals(isMandatory, objectParameter.getIsMandatory());
        assertEquals(linkedObjectType, objectParameter.getLinkedObjectType());
    }

    @Test
    public void testEqualsAndHashCode() {
        ObjectParameter obj = new ObjectParameter();
        assertNotEquals(objectParameter, obj);
        assertNotEquals(objectParameter.hashCode(), obj.hashCode());

        ObjectParameter objectParameter1 = new ObjectParameter();
        assertNotEquals(objectParameter, objectParameter1);
        assertNotEquals(objectParameter.hashCode(), objectParameter1.hashCode());

        BeanUtils.copyProperties(objectParameter, objectParameter1);
        assertEquals(objectParameter, objectParameter1);
        assertEquals(objectParameter.hashCode(), objectParameter1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ObjectParameter("
                        + "name="
                        + name
                        + ", dataType="
                        + dataType
                        + ", description="
                        + description
                        + ", example="
                        + example
                        + ", valueSchema="
                        + valueSchema
                        + ", sensitiveScope="
                        + sensitiveScope
                        + ", isMandatory="
                        + isMandatory
                        + ", linkedObjectType="
                        + linkedObjectType
                        + ")";
        assertEquals(expectedString, objectParameter.toString());
    }
}
