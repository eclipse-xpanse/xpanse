/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.utils;

import com.networknt.schema.JsonMetaSchema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The Class is used to generate a JSONSchema for service input variables. */
@Slf4j
@Component
public class ServiceInputVariablesJsonSchemaGenerator {

    private static final String VARIABLE_TYPE_KEY = "type";
    private static final String VARIABLE_DESCRIPTION_KEY = "description";
    private static final String VARIABLE_EXAMPLE_KEY = "examples";

    /**
     * Generate JsonSchema objects to describe validation rules and metadata information for
     * deployment input variables.
     *
     * @param inputVariables list of InputVariable in registered service
     * @return JsonObjectSchema
     */
    public JsonObjectSchema buildJsonSchemaOfInputVariables(List<InputVariable> inputVariables) {
        JsonObjectSchema jsonObjectSchema = new JsonObjectSchema();
        Map<String, Map<String, Object>> inputVariableJsonSchemaProperties = new HashMap<>();
        List<String> requiredList = new ArrayList<>();
        for (InputVariable variable : inputVariables) {
            if (variable.getKind() == VariableKind.VARIABLE
                    || variable.getKind() == VariableKind.ENV) {
                Map<String, Object> validationProperties = new HashMap<>();

                if (Boolean.TRUE.equals(variable.getMandatory())) {
                    requiredList.add(variable.getName());
                }

                if (!CollectionUtils.isEmpty(variable.getValueSchema())) {
                    validationProperties.putAll(variable.getValueSchema());
                }

                if (Objects.nonNull(variable.getDataType())) {
                    validationProperties.put(VARIABLE_TYPE_KEY, variable.getDataType().toValue());
                }

                if (Objects.nonNull(variable.getDescription())) {
                    validationProperties.put(VARIABLE_DESCRIPTION_KEY, variable.getDescription());
                }

                if (Objects.nonNull(variable.getExample())) {
                    validationProperties.put(VARIABLE_EXAMPLE_KEY, variable.getExample());
                }

                if (!validationProperties.isEmpty()) {
                    inputVariableJsonSchemaProperties.put(variable.getName(), validationProperties);
                }
            }
        }
        jsonObjectSchema.setRequired(requiredList);
        jsonObjectSchema.setProperties(inputVariableJsonSchemaProperties);
        validateSchemaDefinition(jsonObjectSchema);
        return jsonObjectSchema;
    }

    private void validateSchemaDefinition(JsonObjectSchema jsonObjectSchema) {
        Set<String> allowedKeys = JsonMetaSchema.getV202012().getKeywords().keySet();
        List<String> invalidKeys = new ArrayList<>();
        jsonObjectSchema
                .getProperties()
                .forEach(
                        (inputVariable, variableValueSchema) ->
                                variableValueSchema.forEach(
                                        (schemaDefKey, schemaDefValue) -> {
                                            if (!allowedKeys.contains(schemaDefKey)) {
                                                invalidKeys.add(
                                                        String.format(
                                                                "Value schema key %s in input"
                                                                        + " variable %s is invalid",
                                                                schemaDefKey, inputVariable));
                                            }
                                        }));
        if (!invalidKeys.isEmpty()) {
            throw new InvalidValueSchemaException(invalidKeys);
        }
    }
}
