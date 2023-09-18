/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.VariableInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The class is used to validate deployment variables.
 */
@Slf4j
@Component
public class ServiceVariablesJsonSchemaValidator {

    private final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Check validation of deploy property map by list of deployVariables in registered service.
     *
     * @param deployVariables list of deployVariables in registered service
     * @param deployProperty  deploy property map
     */
    public void validateDeployVariables(List<DeployVariable> deployVariables,
                                        Map<String, String> deployProperty,
                                        JsonObjectSchema jsonObjectSchema) {
        if (!CollectionUtils.isEmpty(deployVariables) && !CollectionUtils.isEmpty(deployProperty)) {
            try {
                String jsonObjectSchemaString = jsonMapper.writeValueAsString(jsonObjectSchema);
                Locale.setDefault(Locale.ENGLISH);
                JsonSchemaFactory factory =
                        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
                JsonSchema schema = factory.getSchema(jsonObjectSchemaString);
                String propertyJson = jsonMapper.writeValueAsString(deployProperty);
                JsonNode jsonNode = jsonMapper.readTree(propertyJson);
                Set<ValidationMessage> validate = schema.validate(jsonNode);

                if (!validate.isEmpty()) {
                    List<String> errors = new ArrayList<>();
                    for (ValidationMessage validationMessage : validate) {
                        errors.add(validationMessage.getMessage().substring(2));
                    }
                    throw new VariableInvalidException(errors);
                }
            } catch (JsonProcessingException e) {
                List<String> errors = new ArrayList<>();
                errors.add(e.getMessage());
                throw new VariableInvalidException(errors);
            }
        }
    }
}
