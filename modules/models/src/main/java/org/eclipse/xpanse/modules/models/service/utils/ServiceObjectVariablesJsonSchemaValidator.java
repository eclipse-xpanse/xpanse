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
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectRequestInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The class is used to validate service object parameter. */
@Slf4j
@Component
public class ServiceObjectVariablesJsonSchemaValidator {
    private final ObjectMapper jsonMapper = new ObjectMapper();

    /** Validate service object parameters. */
    public void validateServiceObjectParameters(
            Map<String, Object> updateObjectParameters, JsonObjectSchema jsonObjectSchema) {
        try {
            if (Objects.isNull(jsonObjectSchema)) {
                return;
            }
            String jsonObjectSchemaString = jsonMapper.writeValueAsString(jsonObjectSchema);
            Locale.setDefault(Locale.ENGLISH);
            JsonSchemaFactory factory =
                    JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = factory.getSchema(jsonObjectSchemaString);
            String propertyJson = jsonMapper.writeValueAsString(updateObjectParameters);
            JsonNode jsonNode = jsonMapper.readTree(propertyJson);
            Set<ValidationMessage> validate = schema.validate(jsonNode);
            if (!CollectionUtils.isEmpty(validate)) {
                List<String> errors = new ArrayList<>();
                for (ValidationMessage validationMessage : validate) {
                    errors.add(validationMessage.getMessage().substring(3));
                }
                throw new ServiceObjectRequestInvalidException(errors);
            }
        } catch (JsonProcessingException e) {
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            throw new ServiceObjectRequestInvalidException(errors);
        }
    }
}
