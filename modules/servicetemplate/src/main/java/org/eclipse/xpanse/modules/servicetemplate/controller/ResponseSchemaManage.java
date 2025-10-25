/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.annotation.Nonnull;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.MappableFields;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.CustomBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Bean to build the response schema of an OpenAPI method. */
@Slf4j
@Component
public class ResponseSchemaManage {

    private final SchemaUtils schemaUtils;

    @Autowired
    public ResponseSchemaManage(SchemaUtils schemaUtils) {
        this.schemaUtils = schemaUtils;
    }

    /** Builds the response schema of an OpenAPI method. */
    public ApiResponses buildResponseSchema(
            @Nonnull CustomBody responseBody,
            Class<?> xpanseResponseBodyClass,
            Ocl ocl,
            Components components,
            HttpStatus httpStatus,
            boolean isArray,
            Map<MappableFields, String> mappableFieldsStringMap) {
        log.info("Adding schema for {}", responseBody.getTypeName());
        ApiResponse apiResponse;
        Schema<?> existingComponent =
                schemaUtils.reuseExistingComponentIfExists(components, responseBody.getTypeName());
        if (existingComponent == null) {
            Schema<?> originalSchema = null;
            Schema<?> customSchema = null;
            if (xpanseResponseBodyClass != null) {
                originalSchema =
                        schemaUtils.manageAndAddOriginalTypeToSchema(
                                xpanseResponseBodyClass,
                                components,
                                schemaUtils.getAllFieldsToBeRemoved(
                                        ocl.getServiceControllerConfig()),
                                responseBody.getTypeName(),
                                ocl);
            }
            if (responseBody.getAdditionalType() != null) {
                customSchema =
                        schemaUtils.customSchemaObjectFromJson(
                                responseBody.getAdditionalType(), components);
            }
            if (originalSchema != null) {
                if (customSchema != null) {
                    originalSchema.addProperty(customSchema.getTitle(), customSchema);
                }
            } else {
                if (customSchema != null) {
                    originalSchema = new ObjectSchema();
                    originalSchema.title(responseBody.getTypeName());
                    originalSchema.addProperty(customSchema.getTitle(), customSchema);
                }
            }
            if (originalSchema != null) {
                schemaUtils.replacePropertiesWithCustomName(
                        ocl.getServiceControllerConfig().getStandardNameMappings(), originalSchema);
                schemaUtils.replacePropertiesWithCustomName(
                        mappableFieldsStringMap, originalSchema);
                schemaUtils.generateOpenApiSchemaForXpanseVariableDefinitions(ocl, originalSchema);
                schemaUtils.removeValuesToBeSuppressed(
                        originalSchema,
                        schemaUtils.getAllFieldsToBeRemoved(ocl.getServiceControllerConfig()));
                apiResponse = getApiResponse(responseBody, isArray, httpStatus);
            } else {
                apiResponse = new ApiResponse().description(httpStatus.getReasonPhrase());
            }
        } else {
            log.info("Schema {} already exists. Reusing it.", existingComponent.getName());
            apiResponse = getApiResponse(responseBody, isArray, httpStatus);
        }
        return new ApiResponses().addApiResponse(Integer.toString(httpStatus.value()), apiResponse);
    }

    private Schema<?> finalResponseSchema(String typeName, boolean isArray) {
        if (isArray) {
            Schema<?> arrayWrapper = new ArraySchema();
            arrayWrapper.items(new Schema<>().$ref(schemaUtils.getComponentSchemaPath(typeName)));
            return arrayWrapper;
        } else {
            return new Schema<>().$ref(schemaUtils.getComponentSchemaPath(typeName));
        }
    }

    private ApiResponse getApiResponse(
            CustomBody responseBody, boolean isArray, HttpStatus httpStatus) {
        return new ApiResponse()
                .content(
                        new Content()
                                .addMediaType(
                                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                        new MediaType()
                                                .schema(
                                                        finalResponseSchema(
                                                                responseBody.getTypeName(),
                                                                isArray))))
                .description(httpStatus.getReasonPhrase());
    }
}
