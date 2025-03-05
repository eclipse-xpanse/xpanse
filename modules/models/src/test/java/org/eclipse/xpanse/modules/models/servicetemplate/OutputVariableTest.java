/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/** Test of OutputVariable. */
class OutputVariableTest {

    private final String name = "output_var1";
    private final VariableDataType dataType = VariableDataType.STRING;
    private final String description = "description";
    private final SensitiveScope sensitiveScope = SensitiveScope.ONCE;

    private OutputVariable test;

    @BeforeEach
    void setUp() {
        test = new OutputVariable();
        test.setName(name);
        test.setDataType(dataType);
        test.setDescription(description);
        test.setSensitiveScope(sensitiveScope);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, test.getName());
        assertEquals(dataType, test.getDataType());
        assertEquals(description, test.getDescription());
        assertEquals(sensitiveScope, test.getSensitiveScope());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());

        OutputVariable test1 = new OutputVariable();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        OutputVariable test2 = new OutputVariable();
        BeanUtils.copyProperties(test, test2);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "OutputVariable("
                        + "name="
                        + name
                        + ", dataType="
                        + dataType
                        + ", description="
                        + description
                        + ", sensitiveScope="
                        + sensitiveScope
                        + ")";
        assertEquals(expectedString, test.toString());
    }
}
