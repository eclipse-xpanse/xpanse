/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceInputVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.MappableFields;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ServiceControllerConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceControllerConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Utility methods for creating OpenAPI schema. */
@Slf4j
@Component
public class SchemaUtils {

    private static final List<String> FIELDS_TO_BE_REMOVED =
            List.of("serviceName", "category", "objectType", "actionName");

    private final ServiceConfigurationVariablesJsonSchemaGenerator
            serviceConfigurationVariablesJsonSchemaGenerator;
    private final ServiceInputVariablesJsonSchemaGenerator serviceInputVariablesJsonSchemaGenerator;

    /** Constructor method. */
    @Autowired
    public SchemaUtils(
            ServiceConfigurationVariablesJsonSchemaGenerator
                    serviceConfigurationVariablesJsonSchemaGenerator,
            ServiceInputVariablesJsonSchemaGenerator serviceInputVariablesJsonSchemaGenerator) {
        this.serviceConfigurationVariablesJsonSchemaGenerator =
                serviceConfigurationVariablesJsonSchemaGenerator;
        this.serviceInputVariablesJsonSchemaGenerator = serviceInputVariablesJsonSchemaGenerator;
    }

    /** Method to customize the original xpanse's type for a method according to service's names. */
    public Schema<?> manageAndAddOriginalTypeToSchema(
            Class<?> parentType,
            Components components,
            List<String> fieldsToBeRemoved,
            String customOriginalTypeName) {
        Map<String, Schema> schemas = ModelConverters.getInstance().read(parentType);
        Schema<?> parentSchema = schemas.get(parentType.getSimpleName());
        parentSchema.setTitle(customOriginalTypeName);
        parentSchema.setName(customOriginalTypeName);
        components.addSchemas(customOriginalTypeName, parentSchema);
        addAllChildTypesAsSchemas(components, parentType, fieldsToBeRemoved);
        return parentSchema;
    }

    private void addAllChildTypesAsSchemas(
            Components components, Class<?> parentType, List<String> fieldsToBeRemoved) {
        ResolvedSchema resolved = ModelConverters.getInstance().readAllAsResolvedSchema(parentType);
        resolved.referencedSchemas.forEach(
                (name, schema) -> {
                    if (!name.equals(parentType.getSimpleName())
                            && !fieldsToBeRemoved.contains(name.toLowerCase())) {
                        schema.setTitle(name);
                        components.addSchemas(name, schema);
                    }
                });
    }

    /**
     * This method provides the fields/properties that are not necessary when the service is
     * deployed as a separate controller. The fields to be removed depends on the service template
     * data.
     */
    public List<String> getAllFieldsToBeRemoved(ServiceControllerConfig serviceControllerConfig) {
        List<String> valuesToBeRemoved = new ArrayList<>(FIELDS_TO_BE_REMOVED);
        if (serviceControllerConfig.getServiceControllerMode().getIsRegionSpecificController()) {
            valuesToBeRemoved.add("region");
        }
        if (!serviceControllerConfig
                .getServiceControllerMode()
                .getIsSupportsMultipleHostingTypes()) {
            valuesToBeRemoved.add("serviceHostingType");
        }
        if (!serviceControllerConfig
                .getServiceControllerMode()
                .getIsSupportsMultipleCloudProviders()) {
            valuesToBeRemoved.add("csp");
        }
        if (!serviceControllerConfig.getServiceControllerMode().getIsSupportsMultipleVersions()) {
            valuesToBeRemoved.add("version");
        }
        return valuesToBeRemoved;
    }

    /**
     * This method removes the fields/properties that are not necessary when the service is deployed
     * as a separate controller. The fields to be removed depends on the service template data.
     */
    public void removeValuesToBeSuppressed(Schema<?> schema, List<String> valuesToBeRemoved) {
        if (schema == null) {
            return;
        }
        Map<String, Schema> properties = schema.getProperties();
        valuesToBeRemoved.forEach(
                field -> {
                    if (properties != null) {
                        properties.remove(field);
                        if (schema.getRequired() != null) {
                            schema.getRequired().removeIf(property -> property.equals(field));
                        } // remove old key
                    }
                    if (properties != null) {
                        for (Schema<?> child : properties.values()) {
                            removeValuesToBeSuppressed(child, valuesToBeRemoved);
                        }
                    }
                });
    }

    private Schema<?> buildDynamicObjectSchema(
            String description, Map<String, Map<String, Object>> fields) {

        Schema<?> parent = new ObjectSchema().description(description);

        for (var entry : fields.entrySet()) {
            Map<String, Object> attributes = entry.getValue();

            Schema<?> s = new Schema<>();
            switch ((String) attributes.get("type")) {
                case "string" -> s = new StringSchema();
                case "number" -> s = new IntegerSchema();
                case "boolean" -> s = new BooleanSchema();
                default -> new ObjectSchema().description(description);
            }

            if (attributes.containsKey("description")) {
                s.setDescription((String) attributes.get("description"));
            }
            if (attributes.containsKey("example")) {
                s.setExample(attributes.get("example"));
            }
            if (attributes.containsKey("pattern")) {
                s.setPattern((String) attributes.get("pattern"));
            }
            if (attributes.containsKey("minLength")) {
                s.setMinLength((Integer) attributes.get("minLength"));
            }
            if (attributes.containsKey("maxLength")) {
                s.setMaxLength((Integer) attributes.get("maxLength"));
            }
            parent.addProperty(entry.getKey(), s);
        }

        parent.setAdditionalProperties(false);
        return parent;
    }

    /** This method converts the deployment variables into a OpenAPI schema object. */
    public void generateServiceDeploymentVariables(Ocl ocl, Schema<?> originalSchema) {
        if (originalSchema.getProperties().get("serviceRequestProperties") != null) {
            JsonObjectSchema jsonObjectSchema =
                    serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                            ocl.getDeployment().getTerraformDeployment().getInputVariables());
            Schema<?> deploymentVariables =
                    buildDynamicObjectSchema(
                            ocl.getDescription(), jsonObjectSchema.getProperties());
            originalSchema.addProperty("serviceRequestProperties", deploymentVariables);
        }
    }

    /** This method converts any parameter variables into a OpenAPI schema object. */
    public void generateSchemaForParameterVariables(
            Ocl ocl, Schema<?> originalSchema, String jsonKeyName) {
        if (originalSchema.getProperties().get(jsonKeyName) != null) {
            JsonObjectSchema jsonObjectSchema =
                    serviceConfigurationVariablesJsonSchemaGenerator
                            .buildServiceConfigurationJsonSchema(
                                    ocl.getServiceConfigurationManage()
                                            .getConfigurationParameters());
            Schema<?> configurationProperties =
                    buildDynamicObjectSchema(
                            ocl.getDescription(), jsonObjectSchema.getProperties());
            originalSchema.addProperty(jsonKeyName, configurationProperties);
        }
    }

    /** This method converts a JSON schema string into OpenAPI schema. */
    public Schema<?> customSchemaObjectFromJson(String jsonSchema, Components components) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Schema<?> schema = mapper.readValue(jsonSchema, Schema.class);
            components.addSchemas(schema.getTitle(), schema);
            return schema;
        } catch (JsonProcessingException e) {
            log.error("Error processing the custom schema provided for additional type");
            log.error(e.getMessage(), e);
            throw new ServiceControllerConfigurationInvalidException(List.of(e.getMessage()));
        }
    }

    /** This method replaces property names in a schema with the mappings provided. */
    public void replacePropertiesWithCustomName(
            Map<MappableFields, String> mappableFieldsStringMap, Schema<?> schema) {
        if (mappableFieldsStringMap != null) {
            mappableFieldsStringMap.forEach(
                    (standardName, customNameMapping) -> {
                        renamePropertyRecursive(schema, standardName.toValue(), customNameMapping);
                    });
        }
    }

    /** replace standard names with specific names. */
    public void renamePropertyRecursive(Schema<?> schema, String oldName, String newName) {
        if (schema == null) {
            return;
        }
        // If this schema has properties (object type)
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null && properties.containsKey(oldName)) {
            Schema<?> target = properties.remove(oldName); // remove old key
            properties.put(newName, target); // add new key
        }

        // Recurse into all properties
        if (properties != null) {
            for (Schema<?> child : properties.values()) {
                renamePropertyRecursive(child, oldName, newName);
            }
        }

        // Recurse into array item
        if (schema.getItems() != null) {
            renamePropertyRecursive(schema.getItems(), oldName, newName);
        }

        // Handle composed schemas (allOf / anyOf / oneOf)
        if (schema.getAllOf() != null) {
            schema.getAllOf().forEach(s -> renamePropertyRecursive(s, oldName, newName));
        }
        if (schema.getAnyOf() != null) {
            schema.getAnyOf().forEach(s -> renamePropertyRecursive(s, oldName, newName));
        }
        if (schema.getOneOf() != null) {
            schema.getOneOf().forEach(s -> renamePropertyRecursive(s, oldName, newName));
        }
    }

    /** Method provides the component name link for a schema type. */
    public String getComponentSchemaPath(String schemaName) {
        return "#/components/schemas/" + schemaName;
    }

    /** This method checks and adds unique tags to the OpenAPI . */
    public void addTagIfUnique(String tagName, OpenAPI openApi) {
        if (openApi.getTags() == null) {
            openApi.setTags(new java.util.ArrayList<>());
        }

        boolean exists = openApi.getTags().stream().anyMatch(t -> t.getName().equals(tagName));

        if (!exists) {
            Tag tag = new Tag().name(tagName);
            openApi.getTags().add(tag);
        }
    }

    /**
     * This method adds Operation object to the OpenAPI object. This is necessary to allow adding
     * different methods with same URI. Otherwise, the operation configuration will be overridden.
     */
    public void addOperation(
            OpenAPI openApi, String path, PathItem.HttpMethod method, Operation operation) {
        Paths paths = openApi.getPaths();
        PathItem pathItem = paths.get(path);

        if (pathItem == null) {
            pathItem = new PathItem();
        }

        switch (method) {
            case GET -> pathItem.setGet(operation);
            case POST -> pathItem.setPost(operation);
            case PUT -> pathItem.setPut(operation);
            case DELETE -> pathItem.setDelete(operation);
            case PATCH -> pathItem.setPatch(operation);
            case HEAD -> pathItem.setHead(operation);
            case OPTIONS -> pathItem.setOptions(operation);
            case TRACE -> pathItem.setTrace(operation);
            default -> pathItem.setGet(operation);
        }

        paths.addPathItem(path, pathItem);
    }
}
