/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.xpanse.modules.models.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.enums.DeployVariableType;
import org.eclipse.xpanse.modules.models.enums.VariableValidator;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DeployVariableValidator.class,})
public class DeployVariableValidatorTest {

    @Autowired
    DeployVariableValidator validator;

    @Test
    public void isVariableValid_test() throws Exception {

        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/test.yaml").toURI().toURL());
        List<DeployVariable> variables = ocl.getDeployment().getContext();
        Map<String, String> property = new HashMap<>();
        property.put("HW_REGION_NAME", "cn-southwest-2");
        property.put("cpv_id", "");
        property.put("secgroup_id", null);
        property.put("number_test", "12");
        property.put("string_test", "helloword");
        property.put("enum_test", "red");
        property.put("pattern_test", "pattern_test");
        boolean isValid = validator.isVariableValid(variables, property);
        Assertions.assertTrue(isValid);
    }


    @Test
    public void isVariableValid_ignoreRequired_test() {
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setKind(DeployVariableKind.VARIABLE);
        deployVariable.setName("required_test");
        deployVariable.setMandatory(true);
        List<DeployVariable> variables = new ArrayList<>();
        variables.add(deployVariable);
        Map<String, String> property = new HashMap<>();
        property.put("HW_REGION_NAME", "cn-southwest-2");

        Set<String> ignoredKeys = new HashSet<>();
        ignoredKeys.add("required_test");
        String errorMsg = String.format("Required context of service not"
                + " found keys %s from keys of property", ignoredKeys);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validator.isVariableValid(variables, property), errorMsg);
    }


    @Test
    public void isVariableValid_stringValidator_test() {
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setKind(DeployVariableKind.VARIABLE);
        deployVariable.setName("string_test");
        deployVariable.setMandatory(true);
        deployVariable.setType(DeployVariableType.STRING);
        deployVariable.setValidator("minLength=4|maxLength=10");
        List<DeployVariable> variables = new ArrayList<>();
        variables.add(deployVariable);

        Map<String, String> property = new HashMap<>();
        property.put("string_test", "test");
        Assertions.assertTrue(validator.isVariableValid(variables, property));

        Map<String, String> property1 = new HashMap<>();
        property1.put("string_test", "string_test_12345");
        String errorMsg1 = String.format("Key %s with value %s in is valid. Validator [%s: %s]",
                "string_test", "string_test_12345", VariableValidator.MAXLENGTH.toValue(), 10);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validator.isVariableValid(variables, property1), errorMsg1);

        Map<String, String> property2 = new HashMap<>();
        property2.put("string_test", "a");
        String errorMsg2 = String.format("Key %s with value %s in is valid. Validator [%s: %s]",
                "string_test", "string_test_12345", VariableValidator.MINLENGTH.toValue(), 2);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validator.isVariableValid(variables, property2), errorMsg2);
    }


    @Test
    public void isVariableValid_numberValidator_test() {
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setKind(DeployVariableKind.VARIABLE);
        deployVariable.setName("number_test");
        deployVariable.setMandatory(true);
        deployVariable.setType(DeployVariableType.NUMBER);
        deployVariable.setValidator("minimum=10|maximum=100");
        List<DeployVariable> variables = new ArrayList<>();
        variables.add(deployVariable);

        Map<String, String> property = new HashMap<>();
        property.put("number_test", "15");
        Assertions.assertTrue(validator.isVariableValid(variables, property));

        Map<String, String> property1 = new HashMap<>();
        property1.put("number_test", "8");
        String errorMsg1 = String.format("Key %s with value %s in is valid. Validator [%s: %s]",
                "number_test", "8", VariableValidator.MINIMUM.toValue(), 10);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validator.isVariableValid(variables, property1), errorMsg1);

        Map<String, String> property2 = new HashMap<>();
        property2.put("number_test", "101");
        String errorMsg2 = String.format("Key %s with value %s in is valid. Validator [%s: %s]",
                "string_test", "string_test_12345", VariableValidator.MAXIMUM.toValue(), 100);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validator.isVariableValid(variables, property2), errorMsg2);
    }

    @Test
    public void isVariableValid_enum_test() {
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setKind(DeployVariableKind.VARIABLE);
        deployVariable.setName("enum_test");
        deployVariable.setMandatory(true);
        deployVariable.setType(DeployVariableType.STRING);
        deployVariable.setValidator("enum=[\"red\",\"yellow\",\"green\"]");
        List<DeployVariable> variables = new ArrayList<>();
        variables.add(deployVariable);

        Map<String, String> property = new HashMap<>();
        property.put("enum_test", "red");
        Assertions.assertTrue(validator.isVariableValid(variables, property));

        Map<String, String> property1 = new HashMap<>();
        property.put("enum_test", "red123");
        String errorMsg1 = String.format("Key %s with value %s in is valid. Validator [%s: %s]",
                "enum_test", "red123", VariableValidator.ENUM.toValue(), "[\"red\",\"yellow\","
                        + "\"green\"]");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validator.isVariableValid(variables, property1), errorMsg1);

    }


    @Test
    public void isVariableValid_pattern_test() {
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setKind(DeployVariableKind.VARIABLE);
        deployVariable.setName("pattern_test");
        deployVariable.setMandatory(true);
        deployVariable.setType(DeployVariableType.STRING);
        deployVariable.setValidator("pattern=*abc*");
        List<DeployVariable> variables = new ArrayList<>();
        variables.add(deployVariable);

        Map<String, String> property = new HashMap<>();
        property.put("pattern_test", "123abc");
        Assertions.assertTrue(validator.isVariableValid(variables, property));

        Map<String, String> property1 = new HashMap<>();
        property.put("pattern_test", "red123");
        String errorMsg1 = String.format("Key %s with value %s in is valid. Validator [%s: %s]",
                "pattern_test", "red123", VariableValidator.PATTERN.toValue(), "red123");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validator.isVariableValid(variables, property1), errorMsg1);

    }
}
