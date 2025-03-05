/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.VariableValidationFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test of ServiceVariablesJsonSchemaGenerator and ServiceVariablesJsonSchemaValidator. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
            ServiceInputVariablesJsonSchemaGenerator.class,
            ServiceInputVariablesJsonSchemaValidator.class
        })
class ServiceInputVariablesJsonSchemaGeneratorAndValidatorTest {

    @Autowired ServiceInputVariablesJsonSchemaGenerator serviceInputVariablesJsonSchemaGenerator;

    @Autowired ServiceInputVariablesJsonSchemaValidator serviceInputVariablesJsonSchemaValidator;

    private List<InputVariable> inputVariables;

    @BeforeEach
    void setup() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl =
                oclLoader.getOcl(
                        new File("src/test/resources/ocl_terraform_test.yml").toURI().toURL());
        inputVariables = ocl.getDeployment().getInputVariables();
    }

    @Test
    void validateSuccess_test() {
        JsonObjectSchema jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        inputVariables);

        Map<String, Object> deployProperty = new HashMap<>();
        deployProperty.put("admin_passwd", "123456@Qq");

        assertDoesNotThrow(
                () -> {
                    serviceInputVariablesJsonSchemaValidator.validateInputVariables(
                            inputVariables, deployProperty, jsonObjectSchema);
                });
    }

    @Test
    void validateWithValueSchema_test() {
        JsonObjectSchema jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        inputVariables);

        Map<String, Object> validateMinLengthPro = new HashMap<>();
        validateMinLengthPro.put("admin_passwd", "123456");

        Map<String, Object> validateMaxLengthPro = new HashMap<>();
        validateMaxLengthPro.put("admin_passwd", "1234566778980129342543654756768");

        Map<String, Object> validatePatternPro = new HashMap<>();
        validatePatternPro.put("admin_passwd", "12335435@Q");

        assertThrows(
                VariableValidationFailedException.class,
                () -> {
                    serviceInputVariablesJsonSchemaValidator.validateInputVariables(
                            inputVariables, validateMinLengthPro, jsonObjectSchema);
                });
        assertThrows(
                VariableValidationFailedException.class,
                () -> {
                    serviceInputVariablesJsonSchemaValidator.validateInputVariables(
                            inputVariables, validateMaxLengthPro, jsonObjectSchema);
                });
        assertThrows(
                VariableValidationFailedException.class,
                () -> {
                    serviceInputVariablesJsonSchemaValidator.validateInputVariables(
                            inputVariables, validatePatternPro, jsonObjectSchema);
                });
    }

    @Test
    void validateWithRequiredSuccess_test() {
        for (InputVariable variable : inputVariables) {
            variable.setMandatory(true);
        }

        JsonObjectSchema jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        inputVariables);

        Map<String, Object> validateRequiredPro = new HashMap<>();
        validateRequiredPro.put("admin_passwd", "123456@Qq");
        validateRequiredPro.put("vpc_name", "123456");
        validateRequiredPro.put("subnet_name", "123456");
        validateRequiredPro.put("secgroup_name", "123456");

        assertDoesNotThrow(
                () -> {
                    serviceInputVariablesJsonSchemaValidator.validateInputVariables(
                            inputVariables, validateRequiredPro, jsonObjectSchema);
                });
    }

    @Test
    void validateWithRequiredFail_test() {
        for (InputVariable variable : inputVariables) {
            variable.setMandatory(true);
        }

        JsonObjectSchema jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        inputVariables);

        Map<String, Object> validateRequiredPro = new HashMap<>();
        validateRequiredPro.put("admin_passwd", "123456@Qq");

        assertThrows(
                VariableValidationFailedException.class,
                () -> {
                    serviceInputVariablesJsonSchemaValidator.validateInputVariables(
                            inputVariables, validateRequiredPro, jsonObjectSchema);
                });
    }

    @Test
    void validateWithDataType_test() {
        JsonObjectSchema jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        inputVariables);

        Map<String, Map<String, Object>> properties = jsonObjectSchema.getProperties();
        for (InputVariable variable : inputVariables) {
            Map<String, Object> stringObjectMap = properties.get(variable.getName());
            assertEquals(variable.getDataType().toValue(), stringObjectMap.get("type"));
        }
    }

    @Test
    void throwExceptionWhenValueSchemaIsInvalid() {
        inputVariables.getFirst().getValueSchema().put("enums", List.of(1, 2, 3));
        assertThrows(
                InvalidValueSchemaException.class,
                () -> {
                    serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                            inputVariables);
                });
    }
}
