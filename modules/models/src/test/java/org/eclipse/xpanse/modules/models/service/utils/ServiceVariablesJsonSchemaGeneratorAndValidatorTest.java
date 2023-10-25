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
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.VariableInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
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

/**
 * Test of ServiceVariablesJsonSchemaGenerator and ServiceVariablesJsonSchemaValidator.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceVariablesJsonSchemaGenerator.class,
        ServiceVariablesJsonSchemaValidator.class})
class ServiceVariablesJsonSchemaGeneratorAndValidatorTest {

    @Autowired
    ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;

    @Autowired
    ServiceVariablesJsonSchemaValidator serviceVariablesJsonSchemaValidator;

    private List<DeployVariable> variables;

    @BeforeEach
    void setup() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/test.yaml").toURI().toURL());
        variables = ocl.getDeployment().getVariables();
    }

    @Test
    void validateSuccess_test() {
        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(variables);

        Map<String, Object> deployProperty = new HashMap<>();
        deployProperty.put("admin_passwd", "123456@Qq");

        assertDoesNotThrow(() -> {
            serviceVariablesJsonSchemaValidator.validateDeployVariables(variables, deployProperty,
                    jsonObjectSchema);
        });
    }

    @Test
    void validateWithValueSchema_test() {
        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(variables);

        Map<String, Object> validateMinLengthPro = new HashMap<>();
        validateMinLengthPro.put("admin_passwd", "123456");

        Map<String, Object> validateMaxLengthPro = new HashMap<>();
        validateMaxLengthPro.put("admin_passwd", "1234566778980129342543654756768");

        Map<String, Object> validatePatternPro = new HashMap<>();
        validatePatternPro.put("admin_passwd", "12335435@Q");

        assertThrows(VariableInvalidException.class, () -> {
            serviceVariablesJsonSchemaValidator.validateDeployVariables(variables,
                    validateMinLengthPro,
                    jsonObjectSchema);
        });
        assertThrows(VariableInvalidException.class, () -> {
            serviceVariablesJsonSchemaValidator.validateDeployVariables(variables,
                    validateMaxLengthPro,
                    jsonObjectSchema);
        });
        assertThrows(VariableInvalidException.class, () -> {
            serviceVariablesJsonSchemaValidator.validateDeployVariables(variables,
                    validatePatternPro,
                    jsonObjectSchema);
        });
    }

    @Test
    void validateWithRequiredSuccess_test() {
        for (DeployVariable variable : variables) {
            variable.setMandatory(true);
        }

        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(variables);

        Map<String, Object> validateRequiredPro = new HashMap<>();
        validateRequiredPro.put("admin_passwd", "123456@Qq");
        validateRequiredPro.put("vpc_name", "123456");
        validateRequiredPro.put("subnet_name", "123456");
        validateRequiredPro.put("secgroup_name", "123456");

        assertDoesNotThrow(() -> {
            serviceVariablesJsonSchemaValidator.validateDeployVariables(variables,
                    validateRequiredPro,
                    jsonObjectSchema);
        });
    }

    @Test
    void validateWithRequiredFail_test() {
        for (DeployVariable variable : variables) {
            variable.setMandatory(true);
        }

        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(variables);

        Map<String, Object> validateRequiredPro = new HashMap<>();
        validateRequiredPro.put("admin_passwd", "123456@Qq");

        assertThrows(VariableInvalidException.class, () -> {
            serviceVariablesJsonSchemaValidator.validateDeployVariables(variables,
                    validateRequiredPro,
                    jsonObjectSchema);
        });
    }

    @Test
    void validateWithDataType_test() {
        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(variables);

        Map<String, Map<String, Object>> properties = jsonObjectSchema.getProperties();
        for (DeployVariable variable : variables) {
            Map<String, Object> stringObjectMap = properties.get(variable.getName());
            assertEquals(variable.getDataType().toValue(), stringObjectMap.get("type"));
        }
    }

    @Test
    void throwExceptionWhenValueSchemaIsInvalid() {
        variables.get(0).getValueSchema().put("enums", List.of(1, 2, 3));
        assertThrows(InvalidValueSchemaException.class, () -> {
            serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(variables);
        });
    }

}
