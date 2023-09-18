/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deploy.enums.VariableValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployVariable.
 */
class DeployVariableTest {

    private static final String name = "deployVariable_name";
    private static final DeployVariableKind kind = DeployVariableKind.VARIABLE;
    private static final DeployVariableDataType dataType = DeployVariableDataType.STRING;
    private static final String example = "example";
    private static final String description = "description";
    private static final String value = "value";
    private static final Boolean mandatory = true;
    private static final Map<VariableValidator,Object> validatorMap  = Map.of(VariableValidator.MINLENGTH,"10");
    private static final SensitiveScope sensitiveScope = SensitiveScope.ONCE;
    private static DeployVariable deployVariable;

    @BeforeEach
    void setUp() {
        deployVariable = new DeployVariable();
        deployVariable.setName(name);
        deployVariable.setKind(kind);
        deployVariable.setDataType(dataType);
        deployVariable.setExample(example);
        deployVariable.setDescription(description);
        deployVariable.setValue(value);
        deployVariable.setMandatory(mandatory);
        deployVariable.setValueSchema(validatorMap);
        deployVariable.setSensitiveScope(sensitiveScope);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, deployVariable.getName());
        assertEquals(kind, deployVariable.getKind());
        assertEquals(dataType, deployVariable.getDataType());
        assertEquals(example, deployVariable.getExample());
        assertEquals(description, deployVariable.getDescription());
        assertEquals(value, deployVariable.getValue());
        assertEquals(mandatory, deployVariable.getMandatory());
        assertEquals(validatorMap, deployVariable.getValueSchema());
        assertEquals(sensitiveScope, deployVariable.getSensitiveScope());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(deployVariable, deployVariable);
        assertEquals(deployVariable.hashCode(), deployVariable.hashCode());

        Object obj = new Object();
        assertNotEquals(deployVariable, obj);
        assertNotEquals(deployVariable, null);
        assertNotEquals(deployVariable.hashCode(), obj.hashCode());

        DeployVariable deployVariable1 = new DeployVariable();
        DeployVariable deployVariable2 = new DeployVariable();
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable, deployVariable2);
        assertEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable.hashCode(), deployVariable2.hashCode());
        assertEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setName(name);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setKind(kind);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setDataType(dataType);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setExample(example);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setDescription(description);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setValue(value);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setMandatory(mandatory);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setValueSchema(validatorMap);
        assertNotEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertNotEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());

        deployVariable1.setSensitiveScope(sensitiveScope);
        assertEquals(deployVariable, deployVariable1);
        assertNotEquals(deployVariable1, deployVariable2);
        assertEquals(deployVariable.hashCode(), deployVariable1.hashCode());
        assertNotEquals(deployVariable1.hashCode(), deployVariable2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "DeployVariable(" +
                "name=" + name +
                ", kind=" + kind + "" +
                ", dataType=" + dataType + "" +
                ", example=" + example + "" +
                ", description=" + description + "" +
                ", value=" + value + "" +
                ", mandatory=" + mandatory + "" +
                ", valueSchema=" + validatorMap + "" +
                ", sensitiveScope=" + sensitiveScope + "" +
                ")";
        assertEquals(expectedString, deployVariable.toString());
    }

}
