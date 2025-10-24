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
import io.swagger.v3.oas.models.media.ArraySchema;
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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceInputVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.MappableFields;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ServiceControllerConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.CommonReadDomainType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceControllerConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Utility methods for creating OpenAPI schema. */
@Slf4j
@SuppressWarnings("rawtypes")
@Component
public class SchemaUtils {

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
            String customOriginalTypeName,
            Ocl ocl) {
        Map<String, Schema> schemas = ModelConverters.getInstance().read(parentType);
        Schema<?> parentSchema = schemas.get(parentType.getSimpleName());
        parentSchema.setTitle(customOriginalTypeName);
        parentSchema.setName(customOriginalTypeName);
        components.addSchemas(customOriginalTypeName, parentSchema);
        addAllChildTypesAsSchemas(components, parentType, fieldsToBeRemoved);
        replaceGlobalChildPropertiesWithCustomTypeNames(parentSchema, ocl, components);
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
     * In the OCL, the service provider declares data models and names for different domains of the
     * service. But there are some common services which span across multiple domains. In such
     * cases, the property names must be taken from OCL from respective domains and update the type
     * correspondingly. Example - For configuration management, service provider can define specific
     * names. But when there is a common method for searching all services, it returns also the
     * configuration of the corresponding service. This must map to the custom names and types
     * provided for the configuration of that service.
     */
    private void replaceGlobalChildPropertiesWithCustomTypeNames(
            Schema<?> parentSchema, Ocl ocl, Components components) {
        updateChildNamesAndSchemaRef(
                parentSchema, ocl, components, ServiceConfigurationDetails.class);
        updateChildNamesAndSchemaRef(parentSchema, ocl, components, ServiceOrderDetails.class);
    }

    private void updateChildNamesAndSchemaRef(
            Schema<?> schema, Ocl ocl, Components components, Class<?> globalNameToBeReplaced) {
        // case where the property name is not same as globalTypeName. So we just search using
        // reference and update only the reference. Name itself is not updated.
        if (schema.get$ref() != null
                && schema.get$ref()
                        .equals(getComponentSchemaPath(globalNameToBeReplaced.getSimpleName()))) {
            schema.set$ref(
                    getComponentSchemaPath(
                            mapInternalTypeToCustomType(globalNameToBeReplaced, ocl)));
            components.getSchemas().remove(globalNameToBeReplaced.getSimpleName());
            return;
        }
        // case where schema has no ref as above or no further properties.
        if (schema.getProperties() == null) {
            return;
        }
        // case where property name is same as the global type name. In this case, the property name
        // as well as the schema must be updated.
        if (schema.getProperties()
                .containsKey(StringUtils.uncapitalize(globalNameToBeReplaced.getSimpleName()))) {
            schema.getProperties()
                    .remove(StringUtils.uncapitalize(globalNameToBeReplaced.getSimpleName()));
            schema.getProperties()
                    .put(
                            StringUtils.uncapitalize(getCustomTypeNameForConfiguration(ocl)),
                            new Schema()
                                    .$ref(
                                            getComponentSchemaPath(
                                                    getCustomTypeNameForConfiguration(ocl))));
            components.getSchemas().remove(globalNameToBeReplaced.getSimpleName());
            return;
        }

        for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
            Schema value = entry.getValue();
            if (value.getItems() != null) {
                updateChildNamesAndSchemaRef(
                        value.getItems(), ocl, components, globalNameToBeReplaced);
            }
            if (value.getProperties() != null) {
                updateChildNamesAndSchemaRef(value, ocl, components, globalNameToBeReplaced);
            }
        }
    }

    /**
     * This method provides the fields/properties that are not necessary when the service is
     * deployed as a separate controller. The fields to be removed depends on the service template
     * data.
     */
    public List<String> getAllFieldsToBeRemoved(ServiceControllerConfig serviceControllerConfig) {
        List<String> valuesToBeRemoved = new ArrayList<>(XpanseGlobalNames.FIELDS_TO_BE_REMOVED);
        if (serviceControllerConfig.getServiceControllerMode().getIsRegionSpecificController()) {
            valuesToBeRemoved.add(XpanseGlobalNames.REGION_PROPERTY_NAME);
        }
        if (!serviceControllerConfig
                .getServiceControllerMode()
                .getIsSupportsMultipleHostingTypes()) {
            valuesToBeRemoved.add(XpanseGlobalNames.SERVICE_HOSTING_TYPE_PROPERTY_NAME);
        }
        if (!serviceControllerConfig
                .getServiceControllerMode()
                .getIsSupportsMultipleCloudProviders()) {
            valuesToBeRemoved.add(XpanseGlobalNames.CSP_TYPE_PROPERTY_NAME);
        }
        if (!serviceControllerConfig.getServiceControllerMode().getIsSupportsMultipleVersions()) {
            valuesToBeRemoved.add(XpanseGlobalNames.SERVICE_VERSION_PROPERTY_NAME);
        }
        return valuesToBeRemoved;
    }

    /**
     * This method removes the fields/properties that are not necessary when the service is deployed
     * as a separate controller. The fields to be removed depends on the service template data.
     */
    public void removeValuesToBeSuppressed(Schema<?> schema, List<String> valuesToBeRemoved) {
        if (schema == null || schema.getProperties() == null) {
            return;
        }
        valuesToBeRemoved.forEach(field -> removeFieldFromSchema(schema, field));
    }

    private void removeFieldFromSchema(Schema<?> schema, String fieldName) {
        log.info("removing values to be suppressed for {} from {}", fieldName, schema.getName());
        if (schema.getProperties() != null) {
            if (schema.getProperties().containsKey(fieldName)) {
                schema.getProperties().remove(fieldName);
                if (schema.getRequired() != null) {
                    schema.getRequired().removeIf(property -> property.equals(fieldName));
                }
                // if the entire property is removed, no need to loop the child elements.
            } else {
                for (Schema<?> child : schema.getProperties().values()) {
                    if (child.getProperties() != null) {
                        removeFieldFromSchema(child, fieldName);
                    }
                }
            }
        }
    }

    private Schema<?> buildDynamicObjectSchema(
            String description, Map<String, Map<String, Object>> fields, String schemaName) {

        Schema<?> parent = new ObjectSchema().description(description).name(schemaName);

        for (var entry : fields.entrySet()) {
            Map<String, Object> attributes = entry.getValue();

            Schema<?> s = new Schema<>();
            switch (VariableDataType.getByValue(attributes.get("type").toString())) {
                case VariableDataType.STRING -> s = new StringSchema();
                case VariableDataType.NUMBER -> s = new IntegerSchema();
                case VariableDataType.BOOLEAN -> s = new BooleanSchema();
                case VariableDataType.ARRAY -> s = new ArraySchema();
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

    /** Method manages all properties that are related to xpanse's variable definition in OCL. */
    public void generateOpenApiSchemaForXpanseVariableDefinitions(
            Ocl ocl, Schema<?> originalSchema) {
        log.info("generating schema for variable definitions");
        generateServiceDeploymentVariables(ocl, originalSchema);
        generateSchemaForParameterVariables(
                ocl, originalSchema, XpanseGlobalNames.CONFIGURATION_VALUES_PROPERTY_NAME);
        generateSchemaForParameterVariables(
                ocl, originalSchema, XpanseGlobalNames.ACTION_INPUTS_PROPERTY_NAME);
        generateSchemaForParameterVariables(
                ocl, originalSchema, XpanseGlobalNames.SERVICE_OBJECT_INPUTS_PROPERTY_NAME);
    }

    /** This method converts the deployment variables into a OpenAPI schema object. */
    private void generateServiceDeploymentVariables(Ocl ocl, Schema<?> originalSchema) {
        if (originalSchema.getProperties().get(XpanseGlobalNames.SERVICE_INPUTS_PROPERTY_NAME)
                != null) {
            JsonObjectSchema jsonObjectSchema =
                    serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                            ocl.getDeployment().getTerraformDeployment().getInputVariables());
            Schema<?> deploymentVariables =
                    buildDynamicObjectSchema(
                            ocl.getDescription(),
                            jsonObjectSchema.getProperties(),
                            XpanseGlobalNames.SERVICE_INPUTS_PROPERTY_NAME);
            originalSchema.addProperty(
                    XpanseGlobalNames.SERVICE_INPUTS_PROPERTY_NAME, deploymentVariables);
        }
    }

    /** This method converts any parameter variables into a OpenAPI schema object. */
    private void generateSchemaForParameterVariables(
            Ocl ocl, Schema<?> originalSchema, String jsonKeyName) {
        if (originalSchema.getProperties().get(jsonKeyName) != null) {
            JsonObjectSchema jsonObjectSchema =
                    serviceConfigurationVariablesJsonSchemaGenerator
                            .buildServiceConfigurationJsonSchema(
                                    ocl.getServiceConfigurationManage()
                                            .getConfigurationParameters());
            Schema<?> configurationProperties =
                    buildDynamicObjectSchema(
                            ocl.getDescription(), jsonObjectSchema.getProperties(), jsonKeyName);
            originalSchema.addProperty(jsonKeyName, configurationProperties);
        }
    }

    /** This method converts a JSON schema string into OpenAPI schema. */
    public Schema<?> customSchemaObjectFromJson(String jsonSchema, Components components) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Schema<?> schema = mapper.readValue(jsonSchema, Schema.class);
            if (schema.getName() == null) {
                schema.setName(schema.getTitle());
            }
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
        log.info("replacing global mappable names with custom names");
        if (mappableFieldsStringMap != null) {
            mappableFieldsStringMap.forEach(
                    (standardName, customNameMapping) ->
                            renamePropertyRecursive(
                                    schema, standardName.toValue(), customNameMapping));
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
            openApi.setTags(new ArrayList<>());
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

    private String mapInternalTypeToCustomType(Class<?> type, Ocl ocl) {
        return switch (type.getSimpleName()) {
            case XpanseGlobalNames.SERVICE_ORDER_DETAILS -> getCustomTypeNameForOrders(ocl);
            case XpanseGlobalNames.SERVICE_CONFIGURATION_TYPE_NAME ->
                    getCustomTypeNameForConfiguration(ocl);
            default -> throw new IllegalStateException("Unexpected value: " + type.getSimpleName());
        };
    }

    private String getCustomTypeNameForOrders(Ocl ocl) {
        if (ocl.getServiceControllerConfig() == null
                || ocl.getServiceControllerConfig().getCommonReadMethods() == null) {
            return null;
        }

        return ocl.getServiceControllerConfig().getCommonReadMethods().stream()
                .filter(m -> m.getReadDomainType() == CommonReadDomainType.ORDERS)
                .map(m -> m.getResponseBody().getTypeName())
                .findFirst()
                .orElse(null);
    }

    private String getCustomTypeNameForConfiguration(Ocl ocl) {
        if (ocl.getServiceConfigurationManage() == null
                || ocl.getServiceConfigurationManage().getControllerApiMethods() == null
                || ocl.getServiceConfigurationManage()
                                .getControllerApiMethods()
                                .getApiReadMethodConfigs()
                        == null) {
            return null;
        }

        return ocl
                .getServiceConfigurationManage()
                .getControllerApiMethods()
                .getApiReadMethodConfigs()
                .stream()
                .map(m -> m.getResponseBody().getTypeName())
                .findFirst()
                .orElse(null);
    }

    /**
     * Method checks if a component is already added in the schema and reuses it instead of building
     * it again.
     */
    public Schema<?> reuseExistingComponentIfExists(Components components, String typeName) {
        if (components.getSchemas() == null) {
            return null;
        }
        return components.getSchemas().getOrDefault(typeName, null);
    }
}
