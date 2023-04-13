/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.utils;

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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.utils.DeployVariableValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * OpenApiUtil.
 */
@Slf4j
@Component
public class OpenApiUtil {

    private final DeployVariableValidator deployVariableValidator;
    private final String openapiPath;
    private final Integer port;

    /**
     * OpenApiUtil.
     */
    @Autowired
    public OpenApiUtil(DeployVariableValidator deployVariableValidator,
            @Value("${openapi.path:openapi/}") String openapiPath,
            @Value("${server.port:8080}") Integer port) {
        this.deployVariableValidator = deployVariableValidator;
        this.openapiPath = openapiPath;
        this.port = port;
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


    /**
     * Get openApi url by registered service.
     *
     * @param registerServiceEntity the registered service.
     * @return url of openApi
     */
    public String getOpenApi(RegisterServiceEntity registerServiceEntity) {
        if (Objects.isNull(registerServiceEntity)) {
            throw new IllegalArgumentException("Registered service is null.");
        }
        String id = registerServiceEntity.getId().toString();
        File apiFile = new File(getOpenApiDir(), id + ".html");
        if (apiFile.exists()) {
            return getOpenApiUrl(id);
        } else {
            return createServiceApi(registerServiceEntity);
        }
    }

    /**
     * Get serviceUrl.
     *
     * @return serviceUrl
     */
    public String getServiceUrl() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception e) {
            log.error("Get BaseUrl form CurrentContextPath error.");
        }
        String host = "localhost";
        try {
            InetAddress address = InetAddress.getLocalHost();
            host = address.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Get localHost error,");
        }
        return "http://" + host + ":" + port;
    }

    /**
     * Get openApi Url.
     *
     * @return openApiUrl
     */
    public String getOpenApiUrl(String id) {
        if (openapiPath.endsWith("/")) {
            return getServiceUrl() + "/" + openapiPath + id + ".html";
        }
        return getServiceUrl() + "/" + openapiPath + "/" + id + ".html";
    }

    /**
     * Get openApi file path.
     *
     * @return openApiUrl
     */
    private String getOpenApiDir() {
        String rootPath = System.getProperty("user.dir");
        return rootPath + File.separator + openapiPath;
    }

    /**
     * create OpenApi for registered service .
     *
     * @param registerService Registered services.
     */
    public String createServiceApi(RegisterServiceEntity registerService) {
        // ID of registered service.
        String serviceId = registerService.getId().toString();
        String yamlFileName = serviceId + ".yaml";
        String openApiDir = getOpenApiDir();
        File dir = new File(openApiDir);
        if (!dir.exists()) {
            log.info("Create service openApi dir:{} success:{}", openApiDir, dir.mkdirs());
        }
        File yamlFile = new File(openApiDir, yamlFileName);
        File htmlFile = new File(openApiDir, serviceId + ".html");
        try {
            if (yamlFile.exists()) {
                log.info("Service openApi is generating.serviceId:{}", serviceId);
                Thread.sleep(2000);
                if (htmlFile.exists()) {
                    return getOpenApiUrl(serviceId);
                }
            }
            String apiDocsJson = getApiDocsJson(registerService);
            StringBuilder stdErrOut = new StringBuilder();
            try (FileWriter apiWriter =
                    new FileWriter(openApiDir + File.separator + yamlFileName)) {
                apiWriter.write(apiDocsJson);
            }
            String comm = String.format("java -jar ./lib/openapi-generator-cli.jar generate -i "
                    + "%s" + yamlFileName + " -g html2 -o %s", openApiDir, openApiDir);
            Process exec = Runtime.getRuntime().exec(comm);
            BufferedReader outputReader =
                    new BufferedReader(new InputStreamReader((exec.getErrorStream())));
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
            if (tempHtmlFile.exists()) {
                if (tempHtmlFile.renameTo(htmlFile)) {
                    log.info("Create service openApi html file:{} success.", htmlFile.getName());
                    if (htmlFile.exists()) {
                        return getOpenApiUrl(serviceId);
                    }
                }
            }
            return StringUtils.EMPTY;
        } catch (IOException | InterruptedException ex) {
            log.error("Create service openApi html file error:", ex);
            throw new RuntimeException("Create service openApi html file error.", ex);
        } finally {
            // Delete the temp file named serviceId.yaml
            if (yamlFile.exists()) {
                log.info("Delete service openApi temp yaml file success:{}", yamlFile.delete());
            }
        }
    }

    /**
     * update OpenApi for registered service .
     *
     * @param registerService Registered services.
     */
    @Async("taskExecutor")
    public void updateServiceApi(RegisterServiceEntity registerService) {
        File file = new File(getOpenApiDir(), registerService.getId() + ".html");
        if (file.exists()) {
            log.info("Delete old openApi file:{}, success:{}", file.getName(), file.delete());
        }
        createServiceApi(registerService);
    }

    /**
     * update OpenApi for registered service .
     *
     * @param registerService Registered services.
     */
    @Async("taskExecutor")
    public void generateServiceApi(RegisterServiceEntity registerService) {
        createServiceApi(registerService);
    }

    /**
     * delete OpenApi for registered service using the ID.
     *
     * @param id ID of registered service.
     */
    @Async("taskExecutor")
    public void deleteServiceApi(String id) {
        File file = new File(getOpenApiDir(), id + ".html");
        if (file.exists()) {
            log.info("Delete openApi html file:{}, success:{}", file.getName(), file.delete());
        }
    }


    private String getApiDocsJson(RegisterServiceEntity registerService) {
        if (Objects.isNull(registerService)) {
            return StringUtils.EMPTY;
        }
        // version of OCL.
        String oclVersion = registerService.getOcl().getVersion();
        // service url.
        String serviceUrl = getServiceUrl();
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
        List<DeployVariable> deployVariables = registerService.getOcl().getDeployment()
                .getContext();
        try {
            createRequiredStr = mapper.writeValueAsString(getRequiredFields(new CreateRequest()));
            propertiesStr =
                    mapper.writeValueAsString(
                            deployVariableValidator.getVariableApiInfoMap(deployVariables)).replace(
                            "\"[", "[").replace("]\"", "]").replace("\\", "").replace("'", "");
            propertiesRequiredStr = mapper.writeValueAsString(
                    deployVariableValidator.getRequiredKeySet(deployVariables));
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
                                   "description": "OpenAPI for starting a task to deploy the registered service."
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
                                   "/xpanse/service": {
                                       "post": {
                                           "tags": [
                                               "Service"
                                           ],
                                           "description": "Start a task to deploy registered service.",
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
                oclVersion, serviceUrl, createRequiredStr, category, categoryValuesStr, serviceName,
                serviceVersion, csp, cspValuesStr, propertiesRequiredStr, propertiesStr);
    }


}
