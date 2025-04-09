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
import org.eclipse.xpanse.modules.models.servicetemplate.ObjectParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The Class is used to generate a JSONSchema for service object parameter. */
@Slf4j
@Component
public class ServiceObjectVariablesJsonSchemaGenerator {

    private static final String VARIABLE_TYPE_KEY = "type";
    private static final String VARIABLE_DESCRIPTION_KEY = "description";
    private static final String VARIABLE_EXAMPLE_KEY = "examples";

    /** Generate a JSONSchema for service object parameter. */
    public JsonObjectSchema buildJsonSchemaOfServiceObjectParameters(
            List<ObjectParameter> objectParameters) {
        JsonObjectSchema jsonObjectSchema = new JsonObjectSchema();
        List<String> requiredList = new ArrayList<>();
        Map<String, Map<String, Object>> serviceObjectParametersJsonSchemaProperties =
                new HashMap<>();
        objectParameters.forEach(
                objectParameter -> {
                    Map<String, Object> validationProperties =
                            getValidationPropertiesOfObjectParameter(objectParameter, requiredList);
                    if (!CollectionUtils.isEmpty(validationProperties)) {
                        serviceObjectParametersJsonSchemaProperties.put(
                                objectParameter.getName(), validationProperties);
                    }
                });
        jsonObjectSchema.setRequired(requiredList);
        jsonObjectSchema.setProperties(serviceObjectParametersJsonSchemaProperties);
        jsonObjectSchema.setAdditionalProperties(false);
        validateSchemaDefinition(jsonObjectSchema);
        return jsonObjectSchema;
    }

    private Map<String, Object> getValidationPropertiesOfObjectParameter(
            ObjectParameter objectParameter, List<String> requiredList) {
        Map<String, Object> validationProperties = new HashMap<>();

        if (!CollectionUtils.isEmpty(objectParameter.getValueSchema())) {
            validationProperties.putAll(objectParameter.getValueSchema());
        }

        if (Objects.nonNull(objectParameter.getDataType())) {
            validationProperties.put(VARIABLE_TYPE_KEY, objectParameter.getDataType().toValue());
        }

        if (Objects.nonNull(objectParameter.getDescription())) {
            validationProperties.put(VARIABLE_DESCRIPTION_KEY, objectParameter.getDescription());
        }

        if (Objects.nonNull(objectParameter.getExample())) {
            validationProperties.put(VARIABLE_EXAMPLE_KEY, objectParameter.getExample());
        }

        if (Boolean.TRUE.equals(objectParameter.getIsMandatory())) {
            requiredList.add(objectParameter.getName());
        }
        return validationProperties;
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
