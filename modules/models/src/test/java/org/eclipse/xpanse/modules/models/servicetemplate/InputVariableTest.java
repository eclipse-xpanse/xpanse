/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test of InputVariable. */
class InputVariableTest {

    private final String name = "input_var";
    private final VariableKind kind = VariableKind.VARIABLE;
    private final VariableDataType dataType = VariableDataType.STRING;
    private final String example = "example";
    private final String description = "description";
    private final String value = "value";
    private final Boolean mandatory = true;
    private final Map<String, Object> validatorMap = Map.of("minLength", "10");
    private final SensitiveScope sensitiveScope = SensitiveScope.ONCE;
    @Mock private AutoFill autoFill;
    @Mock private ModificationImpact modificationImpact;

    private InputVariable test;

    @BeforeEach
    void setUp() {
        test = new InputVariable();
        test.setName(name);
        test.setKind(kind);
        test.setDataType(dataType);
        test.setExample(example);
        test.setDescription(description);
        test.setValue(value);
        test.setMandatory(mandatory);
        test.setValueSchema(validatorMap);
        test.setSensitiveScope(sensitiveScope);
        test.setAutoFill(autoFill);
        test.setModificationImpact(modificationImpact);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, test.getName());
        assertEquals(kind, test.getKind());
        assertEquals(dataType, test.getDataType());
        assertEquals(example, test.getExample());
        assertEquals(description, test.getDescription());
        assertEquals(value, test.getValue());
        assertEquals(mandatory, test.getMandatory());
        assertEquals(validatorMap, test.getValueSchema());
        assertEquals(sensitiveScope, test.getSensitiveScope());
        assertEquals(modificationImpact, test.getModificationImpact());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());

        InputVariable test1 = new InputVariable();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        InputVariable test2 = new InputVariable();
        BeanUtils.copyProperties(test, test2);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "InputVariable("
                        + "name="
                        + name
                        + ", kind="
                        + kind
                        + ", dataType="
                        + dataType
                        + ", example="
                        + example
                        + ", description="
                        + description
                        + ", value="
                        + value
                        + ", mandatory="
                        + mandatory
                        + ", valueSchema="
                        + validatorMap
                        + ", sensitiveScope="
                        + sensitiveScope
                        + ", autoFill="
                        + autoFill
                        + ", modificationImpact="
                        + modificationImpact
                        + ")";
        assertEquals(expectedString, test.toString());
    }
}
