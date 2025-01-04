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
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The Class is used to generate a JSONSchema for service configuration variables. */
@Slf4j
@Component
public class ServiceConfigurationVariablesJsonSchemaGenerator {

    private static final String VARIABLE_TYPE_KEY = "type";
    private static final String VARIABLE_DESCRIPTION_KEY = "description";
    private static final String VARIABLE_EXAMPLE_KEY = "examples";

    /**
     * Generate JsonSchema objects to describe validation rules and metadata information for a set
     * of ServiceConfigurationParameter.
     *
     * @param serviceConfigurationParameters list of ServiceConfigurationParameter in registered
     *     service.
     * @return JsonObjectSchema jsonObjectSchema.
     */
    public JsonObjectSchema buildServiceConfigurationJsonSchema(
            List<ServiceChangeParameter> serviceConfigurationParameters) {
        JsonObjectSchema jsonObjectSchema = new JsonObjectSchema();
        Map<String, Map<String, Object>> serviceConfigurationJsonSchemaProperties = new HashMap<>();
        for (ServiceChangeParameter configurationParameter : serviceConfigurationParameters) {
            if (configurationParameter.getKind() == DeployVariableKind.VARIABLE
                    || configurationParameter.getKind() == DeployVariableKind.ENV) {
                Map<String, Object> validationProperties = new HashMap<>();

                if (!CollectionUtils.isEmpty(configurationParameter.getValueSchema())) {
                    validationProperties.putAll(configurationParameter.getValueSchema());
                }

                if (Objects.nonNull(configurationParameter.getDataType())) {
                    validationProperties.put(
                            VARIABLE_TYPE_KEY, configurationParameter.getDataType().toValue());
                }

                if (Objects.nonNull(configurationParameter.getDescription())) {
                    validationProperties.put(
                            VARIABLE_DESCRIPTION_KEY, configurationParameter.getDescription());
                }

                if (Objects.nonNull(configurationParameter.getExample())) {
                    validationProperties.put(
                            VARIABLE_EXAMPLE_KEY, configurationParameter.getExample());
                }

                if (!validationProperties.isEmpty()) {
                    serviceConfigurationJsonSchemaProperties.put(
                            configurationParameter.getName(), validationProperties);
                }
            }
        }

        jsonObjectSchema.setProperties(serviceConfigurationJsonSchemaProperties);
        jsonObjectSchema.setAdditionalProperties(false);
        validateSchemaDefinition(jsonObjectSchema);
        return jsonObjectSchema;
    }

    private void validateSchemaDefinition(JsonObjectSchema jsonObjectSchema) {
        Set<String> allowedKeys = JsonMetaSchema.getV202012().getKeywords().keySet();
        List<String> invalidKeys = new ArrayList<>();
        jsonObjectSchema
                .getProperties()
                .forEach(
                        (deployVariable, variableValueSchema) ->
                                variableValueSchema.forEach(
                                        (schemaDefKey, schemaDefValue) -> {
                                            if (!allowedKeys.contains(schemaDefKey)) {
                                                invalidKeys.add(
                                                        String.format(
                                                                "Value schema key %s in deploy"
                                                                        + " variable %s is invalid",
                                                                schemaDefKey, deployVariable));
                                            }
                                        }));
        if (!invalidKeys.isEmpty()) {
            throw new InvalidValueSchemaException(invalidKeys);
        }
    }
}
