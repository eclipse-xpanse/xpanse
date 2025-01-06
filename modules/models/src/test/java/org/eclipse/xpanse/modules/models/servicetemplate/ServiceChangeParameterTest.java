/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of DeployVariable. */
class ServiceChangeParameterTest {

    private static final String name = "service_configuration_parameter_name";
    private static final DeployVariableKind kind = DeployVariableKind.VARIABLE;
    private static final DeployVariableDataType dataType = DeployVariableDataType.STRING;
    private static final String example = "example";
    private static final String description = "description";
    private static final String value = "value";
    private static final String initialValue = "initialValue";
    private static final Map<String, Object> validatorMap = Map.of("minLength", "10");
    private static final SensitiveScope sensitiveScope = SensitiveScope.ONCE;
    private static final String managedBy = "kafka-broker";
    private static ServiceChangeParameter serviceChangeParameter;
    private static ModificationImpact modificationImpact;

    @BeforeEach
    void setUp() {
        serviceChangeParameter = new ServiceChangeParameter();
        serviceChangeParameter.setName(name);
        serviceChangeParameter.setKind(kind);
        serviceChangeParameter.setDataType(dataType);
        serviceChangeParameter.setExample(example);
        serviceChangeParameter.setDescription(description);
        serviceChangeParameter.setValue(value);
        serviceChangeParameter.setInitialValue(initialValue);
        serviceChangeParameter.setValueSchema(validatorMap);
        serviceChangeParameter.setSensitiveScope(sensitiveScope);
        serviceChangeParameter.setModificationImpact(modificationImpact);
        serviceChangeParameter.setIsReadOnly(true);
        serviceChangeParameter.setManagedBy(managedBy);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, serviceChangeParameter.getName());
        assertEquals(kind, serviceChangeParameter.getKind());
        assertEquals(dataType, serviceChangeParameter.getDataType());
        assertEquals(example, serviceChangeParameter.getExample());
        assertEquals(description, serviceChangeParameter.getDescription());
        assertEquals(value, serviceChangeParameter.getValue());
        assertEquals(initialValue, serviceChangeParameter.getInitialValue());
        assertEquals(validatorMap, serviceChangeParameter.getValueSchema());
        assertEquals(sensitiveScope, serviceChangeParameter.getSensitiveScope());
        assertEquals(modificationImpact, serviceChangeParameter.getModificationImpact());
        assertEquals(true, serviceChangeParameter.getIsReadOnly());
        assertEquals(managedBy, serviceChangeParameter.getManagedBy());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(serviceChangeParameter, serviceChangeParameter);
        assertEquals(serviceChangeParameter.hashCode(), serviceChangeParameter.hashCode());

        Object obj = new Object();
        assertNotEquals(serviceChangeParameter, obj);
        assertNotEquals(serviceChangeParameter, null);
        assertNotEquals(serviceChangeParameter.hashCode(), obj.hashCode());

        ServiceChangeParameter configurationParameter1 = new ServiceChangeParameter();
        ServiceChangeParameter configurationParameter2 = new ServiceChangeParameter();
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(serviceChangeParameter, configurationParameter2);
        assertEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter2.hashCode());
        assertEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setName(name);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setKind(kind);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setDataType(dataType);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setExample(example);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setDescription(description);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setValue(value);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setInitialValue(initialValue);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setValueSchema(validatorMap);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setSensitiveScope(sensitiveScope);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setModificationImpact(modificationImpact);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setIsReadOnly(true);
        assertNotEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setManagedBy(managedBy);
        assertEquals(serviceChangeParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertEquals(serviceChangeParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ServiceChangeParameter("
                        + "name="
                        + name
                        + ", kind="
                        + kind
                        + ""
                        + ", dataType="
                        + dataType
                        + ""
                        + ", example="
                        + example
                        + ""
                        + ", description="
                        + description
                        + ""
                        + ", value="
                        + value
                        + ""
                        + ", initialValue="
                        + initialValue
                        + ""
                        + ", valueSchema="
                        + validatorMap
                        + ""
                        + ", sensitiveScope="
                        + sensitiveScope
                        + ""
                        + ", modificationImpact="
                        + modificationImpact
                        + ""
                        + ", isReadOnly="
                        + true
                        + ""
                        + ", managedBy="
                        + managedBy
                        + ""
                        + ")";
        assertEquals(expectedString, serviceChangeParameter.toString());
    }
}
