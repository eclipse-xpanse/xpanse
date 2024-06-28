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

/**
 * Test of DeployVariable.
 */
class ServiceConfigurationParameterTest {

    private static final String name = "service_configuration_parameter_name";
    private static final DeployVariableKind kind = DeployVariableKind.VARIABLE;
    private static final DeployVariableDataType dataType = DeployVariableDataType.STRING;
    private static final String example = "example";
    private static final String description = "description";
    private static final String value = "value";
    private static final String initialValue = "initialValue";
    private static final Boolean mandatory = true;
    private static final Map<String,Object> validatorMap  = Map.of("minLength","10");
    private static final SensitiveScope sensitiveScope = SensitiveScope.ONCE;
    private static ServiceConfigurationParameter serviceConfigurationParameter;
    private static AutoFill autoFill = null;
    private static ModificationImpact modificationImpact;

    @BeforeEach
    void setUp() {
        serviceConfigurationParameter = new ServiceConfigurationParameter();
        serviceConfigurationParameter.setName(name);
        serviceConfigurationParameter.setKind(kind);
        serviceConfigurationParameter.setDataType(dataType);
        serviceConfigurationParameter.setExample(example);
        serviceConfigurationParameter.setDescription(description);
        serviceConfigurationParameter.setValue(value);
        serviceConfigurationParameter.setInitialValue(initialValue);
        serviceConfigurationParameter.setMandatory(mandatory);
        serviceConfigurationParameter.setValueSchema(validatorMap);
        serviceConfigurationParameter.setSensitiveScope(sensitiveScope);
        serviceConfigurationParameter.setAutoFill(autoFill);
        serviceConfigurationParameter.setModificationImpact(modificationImpact);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, serviceConfigurationParameter.getName());
        assertEquals(kind, serviceConfigurationParameter.getKind());
        assertEquals(dataType, serviceConfigurationParameter.getDataType());
        assertEquals(example, serviceConfigurationParameter.getExample());
        assertEquals(description, serviceConfigurationParameter.getDescription());
        assertEquals(value, serviceConfigurationParameter.getValue());
        assertEquals(initialValue, serviceConfigurationParameter.getInitialValue());
        assertEquals(mandatory, serviceConfigurationParameter.getMandatory());
        assertEquals(validatorMap, serviceConfigurationParameter.getValueSchema());
        assertEquals(sensitiveScope, serviceConfigurationParameter.getSensitiveScope());
        assertEquals(modificationImpact, serviceConfigurationParameter.getModificationImpact());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(serviceConfigurationParameter, serviceConfigurationParameter);
        assertEquals(serviceConfigurationParameter.hashCode(), serviceConfigurationParameter.hashCode());

        Object obj = new Object();
        assertNotEquals(serviceConfigurationParameter, obj);
        assertNotEquals(serviceConfigurationParameter, null);
        assertNotEquals(serviceConfigurationParameter.hashCode(), obj.hashCode());

        ServiceConfigurationParameter configurationParameter1 = new ServiceConfigurationParameter();
        ServiceConfigurationParameter configurationParameter2 = new ServiceConfigurationParameter();
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(serviceConfigurationParameter, configurationParameter2);
        assertEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter2.hashCode());
        assertEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setName(name);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setKind(kind);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setDataType(dataType);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setExample(example);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setDescription(description);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setValue(value);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setInitialValue(initialValue);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setMandatory(mandatory);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setValueSchema(validatorMap);
        assertNotEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertNotEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setSensitiveScope(sensitiveScope);
        assertEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setAutoFill(autoFill);
        assertEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());

        configurationParameter1.setModificationImpact(modificationImpact);
        assertEquals(serviceConfigurationParameter, configurationParameter1);
        assertNotEquals(configurationParameter1, configurationParameter2);
        assertEquals(serviceConfigurationParameter.hashCode(), configurationParameter1.hashCode());
        assertNotEquals(configurationParameter1.hashCode(), configurationParameter2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceConfigurationParameter(" +
                "name=" + name +
                ", kind=" + kind + "" +
                ", dataType=" + dataType + "" +
                ", example=" + example + "" +
                ", description=" + description + "" +
                ", value=" + value + "" +
                ", initialValue=" + initialValue + "" +
                ", mandatory=" + mandatory + "" +
                ", valueSchema=" + validatorMap + "" +
                ", sensitiveScope=" + sensitiveScope + "" +
                ", autoFill=" + autoFill + "" +
                ", modificationImpact=" + modificationImpact + "" +
                ")";
        assertEquals(expectedString, serviceConfigurationParameter.toString());
    }

}
