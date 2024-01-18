/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.eclipse.xpanse.modules.async.TaskConfiguration;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Bean to generate OpenApi files for registered services.
 */
@Component
@Slf4j
public class ServiceTemplateOpenApiGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OPENAPI_FILE_EXTENSION = ".html";
    private static final String OPENAPI_EXAMPLE_KEYWORD = "example";
    private static final String JSON_SCHEMA_DEF_EXAMPLE_KEYWORD = "examples";
    private final OpenApiUrlManage openApiUrlManage;
    private final OpenApiGeneratorJarManage openApiGeneratorJarManage;

    /**
     * Constructor to instantiate ServiceTemplateOpenApiGenerator bean.
     *
     * @param openApiUrlManage          OpenApiUrlManage bean
     * @param openApiGeneratorJarManage OpenApiGeneratorJarManage bean
     */
    @Autowired
    public ServiceTemplateOpenApiGenerator(OpenApiUrlManage openApiUrlManage,
                                           OpenApiGeneratorJarManage openApiGeneratorJarManage) {
        this.openApiUrlManage = openApiUrlManage;
        this.openApiGeneratorJarManage = openApiGeneratorJarManage;
    }

    /**
     * Get openApi url by registered service.
     *
     * @param serviceTemplateEntity the registered service.
     * @return url of openApi
     */
    public String getOpenApi(ServiceTemplateEntity serviceTemplateEntity) {
        if (Objects.isNull(serviceTemplateEntity)) {
            throw new ServiceTemplateNotRegistered("Registered service is null.");
        }
        String id = serviceTemplateEntity.getId().toString();
        File apiFile = new File(this.openApiGeneratorJarManage.getOpenApiWorkdir(),
                id + OPENAPI_FILE_EXTENSION);
        if (apiFile.exists()) {
            return this.openApiUrlManage.getOpenApiUrl(id);
        } else {
            return createServiceApi(serviceTemplateEntity);
        }
    }

    /**
     * Generate OpenApi document for registered service.
     *
     * @param registerService Registered services.
     */
    @Async(TaskConfiguration.ASYNC_EXECUTOR_NAME)
    public void generateServiceApi(ServiceTemplateEntity registerService) {
        createServiceApi(registerService);
    }

    /**
     * Update OpenApi document for registered service.
     *
     * @param registerService Registered services.
     */
    @Async(TaskConfiguration.ASYNC_EXECUTOR_NAME)
    public void updateServiceApi(ServiceTemplateEntity registerService) {
        File file =
                new File(this.openApiGeneratorJarManage.getOpenApiWorkdir(),
                        registerService.getId() + OPENAPI_FILE_EXTENSION);
        if (file.exists()) {
            log.info("Delete old openApi file:{}, success:{}", file.getName(), file.delete());
        }
        createServiceApi(registerService);
    }

    /**
     * Delete OpenApi document for registered service using the ID.
     *
     * @param id ID of registered service.
     */
    @Async(TaskConfiguration.ASYNC_EXECUTOR_NAME)
    public void deleteServiceApi(String id) {
        File file = new File(this.openApiGeneratorJarManage.getOpenApiWorkdir(),
                id + OPENAPI_FILE_EXTENSION);
        if (file.exists()) {
            log.info("Delete openApi html file:{}, success:{}", file.getName(), file.delete());
        }
    }

    /**
     * create OpenApi for registered service.
     *
     * @param registerService Registered services.
     */
    public String createServiceApi(ServiceTemplateEntity registerService) {
        // ID of registered service.
        String serviceId = registerService.getId().toString();
        String yamlFileName = serviceId + ".yaml";
        String openApiDir = this.openApiGeneratorJarManage.getOpenApiWorkdir();
        File yamlFile = new File(openApiDir, yamlFileName);
        File htmlFile = new File(openApiDir, serviceId + OPENAPI_FILE_EXTENSION);
        try {
            if (yamlFile.exists()) {
                log.info("Service openApi is being generated. serviceId:{}", serviceId);
                return this.openApiUrlManage.getOpenApiUrl(serviceId);
            } else {
                String apiDocsJson = getApiDocsJson(registerService);
                try (FileWriter apiWriter = new FileWriter(yamlFile.getPath())) {
                    apiWriter.write(apiDocsJson);
                }
                log.info("Service openApi yamlFile:{} create successful.", yamlFile.getPath());
            }
            File jarPath = getJarPath();
            if (yamlFile.exists() && jarPath.exists()) {
                String comm = String.format("java -jar %s generate -g html2 "
                        + "-i %s -o %s", jarPath.getPath(), yamlFile.getPath(), openApiDir);
                Process exec = Runtime.getRuntime().exec(comm.split("\\s+"));
                StringBuilder stdErrOut = new StringBuilder();
                BufferedReader outputReader =
                        new BufferedReader(new InputStreamReader(exec.getErrorStream()));
                String line;
                while ((line = outputReader.readLine()) != null) {
                    stdErrOut.append(line);
                    log.error(line);
                }
                exec.waitFor();
                if (exec.exitValue() != 0) {
                    log.error("Create service openApi html file failed." + stdErrOut);
                }
                // Modify the file name to serviceId.html
                File tempHtmlFile = new File(openApiDir, "index.html");
                if (tempHtmlFile.exists() && (tempHtmlFile.renameTo(htmlFile))) {
                    log.info("Created service openApi html file:{} successfully.",
                            htmlFile.getName());
                    if (htmlFile.exists()) {
                        return this.openApiUrlManage.getOpenApiUrl(serviceId);
                    }

                }
            } else {
                log.error("Not generating {} file. Missing json or openapi-generator jar file",
                        htmlFile.getName());
            }
            return StringUtils.EMPTY;
        } catch (IOException | InterruptedException | RuntimeException ex) {
            log.error("Create service openApi html file error:", ex);
            throw new OpenApiFileGenerationException(
                    "Create service openApi html file error: " + ex.getMessage());
        } finally {
            // Delete the temp file named serviceId.yaml
            if (yamlFile.exists()) {
                try {
                    Files.delete(yamlFile.toPath());
                    log.info("Deleted temp json file:{} successfully.", yamlFile.getName());
                } catch (IOException ioException) {
                    log.info("Deleting temp json file:{} failed.", yamlFile.getName(), ioException);

                }
            }
        }
    }

    /**
     * Get the path of the openapi-generator-cli.jar used.
     *
     * @return File The openapi-generator-cli.jar path.
     */
    private File getJarPath()
            throws IOException {
        return this.openApiGeneratorJarManage.getCliFile();
    }


    private String getApiDocsJson(ServiceTemplateEntity registerService) {
        if (Objects.isNull(registerService)) {
            return StringUtils.EMPTY;
        }
        // service url.
        String serviceUrl = this.openApiUrlManage.getServiceUrl();
        // string of required fields list.
        String createRequiredStr = null;
        // category value of registered service.
        String category = registerService.getCategory().toValue();
        // string of category values list.
        String categoryValuesStr = null;
        // name of registered service.
        String serviceName = registerService.getName();
        // version of registered service.
        String serviceVersion = registerService.getVersion();
        // csp value of registered service.
        String csp = registerService.getCsp().toValue();
        // string of csp values list.
        String cspValuesStr = null;
        // properties for deploy service
        String propertiesStr = null;
        // string of required properties.
        String propertiesRequiredStr = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            createRequiredStr = mapper.writeValueAsString(getRequiredFields(new DeployRequest()));
            propertiesStr = objectMapper.writeValueAsString(
                    convertJsonSchemaSpecToOpenApiSpec(
                            registerService.getJsonObjectSchema().getProperties()));
            propertiesRequiredStr =
                    objectMapper.writeValueAsString(
                            registerService.getJsonObjectSchema().getRequired());
            cspValuesStr = mapper.writeValueAsString(getCspValues());
            categoryValuesStr = mapper.writeValueAsString(getCategoryValues());
        } catch (JsonProcessingException e) {
            log.error("Failed to write value as string.", e);
        }
        //CHECKSTYLE OFF: LineLength
        return String.format("""
                        {
                               "openapi": "3.0.1",
                               "info": {
                                   "title": "OpenAPI definition",
                                   "version": "%s",
                                   "description": "OpenAPI for starting a task to deploy service using registered service template."
                               },
                               "servers": [
                                   {
                                       "url": "%s",
                                       "description": "Generated server url"
                                   }
                               ],
                               "tags": [
                                   {
                                       "name": "Service Vendor",
                                       "description": "APIs to manage register services."
                                   }
                               ],
                               "paths": {
                                   "/xpanse/services": {
                                       "post": {
                                           "tags": [
                                               "Service"
                                           ],
                                           "description": "Start a task to deploy service using registered service template.",
                                           "operationId": "deploy",
                                           "requestBody": {
                                               "content": {
                                                   "application/json": {
                                                       "schema": {
                                                           "$ref": "#/components/schemas/CreateRequest"
                                                       }
                                                   }
                                               },
                                               "required": true
                                           },
                                           "responses": {
                                               "202": {
                                                   "description": "Accepted",
                                                   "content": {
                                                       "application/json": {
                                                           "schema": {
                                                               "type": "string",
                                                               "format": "uuid"
                                                           }
                                                       }
                                                   }
                                               },
                                               
                                               "400": {
                                                   "description": "Bad Request",
                                                   "content": {
                                                       "*/*": {
                                                           "schema": {
                                                               "$ref": "#/components/schemas/Response"
                                                           }
                                                       }
                                                   }
                                               },
                                               "404": {
                                                   "description": "Not Found",
                                                   "content": {
                                                       "*/*": {
                                                           "schema": {
                                                               "$ref": "#/components/schemas/Response"
                                                           }
                                                       }
                                                   }
                                               },
                                               "500": {
                                                   "description": "Internal Server Error",
                                                   "content": {
                                                       "*/*": {
                                                           "schema": {
                                                               "$ref": "#/components/schemas/Response"
                                                           }
                                                       }
                                                   }
                                               }
                                           }
                                       }
                                   }
                               },
                               "components": {
                                   "schemas": {
                                       "Response": {
                                           "required": [
                                               "code",
                                               "message",
                                               "success"
                                           ],
                                           "type": "object",
                                           "properties": {
                                               "code": {
                                                   "type": "string",
                                                   "description": "The result code of response."
                                               },
                                               "message": {
                                                   "type": "string",
                                                   "description": "The result message of response."
                                               },
                                               "success": {
                                                   "type": "boolean",
                                                   "description": "The success boolean of response."
                                               }
                                           }
                                       },
                                       "CreateRequest": {
                                           "required": %s,
                                           "type": "object",
                                           "properties": {
                                               "category": {
                                                   "default": "%s",
                                                   "type": "string",
                                                   "description": "The category of the service",
                                                   "enum": %s
                                               },
                                               "serviceName": {
                                                   "default": "%s",
                                                   "type": "string",
                                                   "description": "The name of the service ordered."
                                               },
                                               "customerServiceName": {
                                                   "type": "string",
                                                   "description": "Customer's name for the service. Used only for customer's reference.If not provided, this value will be auto-generated"
                                               },
                                               "version": {
                                                   "default": "%s",
                                                   "type": "string",
                                                   "description": "The version of service"
                                               },
                                               "region": {
                                                   "type": "string",
                                                   "description": "The region of the provider."
                                               },
                                               "csp": {
                                                   "default": "%s",
                                                   "type": "string",
                                                   "description": "The csp of the Service.",
                                                   "enum": %s
                                               },
                                               "flavor": {
                                                   "type": "string",
                                                   "description": "The flavor of the Service."
                                               },
                                               "property": {
                                                   "$ref": "#/components/schemas/property"
                                               }
                                           }
                                       },
                                       "property": {
                                           "required": %s,
                                           "description": "The deploy variables of registered service.",
                                           "type": "object",
                                           "properties": %s
                                       }
                                   }
                               }
                           }
                          """,
                serviceVersion, serviceUrl, createRequiredStr, category, categoryValuesStr,
                serviceName,
                serviceVersion, csp, cspValuesStr, propertiesRequiredStr, propertiesStr);
    }

    private List<String> getRequiredFields(Object object) {
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            Annotation notNullAnnotation = field.getAnnotation(NotNull.class);
            if (Objects.nonNull(notNullAnnotation)) {
                fieldNames.add(field.getName());

            }
        }
        return fieldNames;
    }

    private List<String> getCspValues() {
        return Arrays.stream(Csp.values()).map(Csp::toValue).collect(Collectors.toList());
    }

    private List<String> getCategoryValues() {
        return Arrays.stream(Category.values()).map(Category::toValue).collect(Collectors.toList());
    }

    private Map<String, Map<String, Object>> convertJsonSchemaSpecToOpenApiSpec(
            Map<String, Map<String, Object>> properties) {
        properties.forEach((key, value) -> {
                    String exampleValue = (String) value.get(JSON_SCHEMA_DEF_EXAMPLE_KEYWORD);
                    if (Objects.nonNull(exampleValue)) {
                        value.remove(JSON_SCHEMA_DEF_EXAMPLE_KEYWORD);
                        value.put(OPENAPI_EXAMPLE_KEYWORD, exampleValue);
                    }
                }
        );
        return properties;
    }
}
