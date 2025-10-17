/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ApiWriteMethodConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Bean to build the request schema of an OpenAPI method. */
@Component
public class RequestSchemaManage {

    private final SchemaUtils schemaUtils;

    /** constructor method. */
    @Autowired
    public RequestSchemaManage(SchemaUtils schemaUtils) {
        this.schemaUtils = schemaUtils;
    }

    /** Builds the request schema of an OpenAPI method. */
    public RequestBody buildRequestBody(
            ApiWriteMethodConfig apiWriteMethodConfig,
            Class<?> xpanseRequestBodyClass,
            Ocl ocl,
            Components components) {
        if (apiWriteMethodConfig.getRequestBody() != null) {
            Schema<?> originalSchema = null;
            if (xpanseRequestBodyClass != null) {
                // we take the original datatype and then update it's name to match the custom name
                // requested in OCL.
                // Then to this schema we add a new property mentioned in the OCL.
                originalSchema =
                        schemaUtils.manageAndAddOriginalTypeToSchema(
                                xpanseRequestBodyClass,
                                components,
                                schemaUtils.getAllFieldsToBeRemoved(
                                        ocl.getServiceControllerConfig()),
                                apiWriteMethodConfig.getRequestBody().getTypeName());
            }
            Schema<?> customSchema = null;
            if (apiWriteMethodConfig.getRequestBody() != null
                    && apiWriteMethodConfig.getRequestBody().getAdditionalType() != null) {
                customSchema =
                        schemaUtils.customSchemaObjectFromJson(
                                apiWriteMethodConfig.getRequestBody().getAdditionalType(),
                                components);
            }
            if (originalSchema != null) {
                if (customSchema != null) {
                    originalSchema.addProperty(customSchema.getTitle(), customSchema);
                }
            } else if (customSchema != null) {
                originalSchema = new ObjectSchema();
                originalSchema.title(apiWriteMethodConfig.getRequestBody().getTypeName());
                originalSchema.addProperty(customSchema.getTitle(), customSchema);
            }

            if (originalSchema != null) {
                schemaUtils.replacePropertiesWithCustomName(
                        ocl.getServiceControllerConfig().getStandardNameMappings(), originalSchema);
                schemaUtils.generateServiceDeploymentVariables(ocl, originalSchema);
                schemaUtils.generateSchemaForParameterVariables(
                        ocl, originalSchema, "configuration");
                schemaUtils.generateSchemaForParameterVariables(
                        ocl, originalSchema, "actionParameters");
                schemaUtils.generateSchemaForParameterVariables(
                        ocl, originalSchema, "serviceObjectParameters");
                schemaUtils.removeValuesToBeSuppressed(
                        originalSchema,
                        schemaUtils.getAllFieldsToBeRemoved(ocl.getServiceControllerConfig()));
            }

            return new RequestBody()
                    .required(true)
                    .content(
                            new Content()
                                    .addMediaType(
                                            org.springframework.http.MediaType
                                                    .APPLICATION_JSON_VALUE,
                                            new MediaType()
                                                    .schema(
                                                            new Schema<>()
                                                                    .$ref(
                                                                            schemaUtils
                                                                                    .getComponentSchemaPath(
                                                                                            apiWriteMethodConfig
                                                                                                    .getRequestBody()
                                                                                                    .getTypeName())))));
        }
        return null;
    }
}
