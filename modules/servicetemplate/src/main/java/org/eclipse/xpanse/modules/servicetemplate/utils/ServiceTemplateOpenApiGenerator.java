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
import java.util.HashMap;
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
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.auth.zitadel.ZitadelIdentityProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to generate OpenApi files for registered services. */
@Component
@Slf4j
public class ServiceTemplateOpenApiGenerator {

    private static final String OPENAPI_FILE_EXTENSION = ".html";
    private static final String OPENAPI_EXAMPLE_KEYWORD = "example";
    private static final String OPENAPI_DESCRIPTION_KEYWORD = "description";
    private static final String OPENAPI_TYPE_KEYWORD = "type";
    private static final String JSON_SCHEMA_DEF_EXAMPLE_KEYWORD = "examples";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenApiUrlManage openApiUrlManage;
    private final OpenApiGeneratorJarManage openApiGeneratorJarManage;
    private final PluginManager pluginManager;
    private final ZitadelIdentityProviderService zitadelIdentityProviderService;
    private final String appVersion;

    @Value("${enable.web.security:false}")
    private Boolean webSecurityIsEnabled;

    @Value("${enable.role.protection:false}")
    private Boolean roleProtectionIsEnabled;

    /** Constructor to instantiate ServiceTemplateOpenApiGenerator bean. */
    @Autowired
    public ServiceTemplateOpenApiGenerator(
            @Value("${app.version:1.0.0}") String appVersion,
            @Nullable ZitadelIdentityProviderService zitadelIdentityProviderService,
            PluginManager pluginManager,
            OpenApiUrlManage openApiUrlManage,
            OpenApiGeneratorJarManage openApiGeneratorJarManage) {
        this.appVersion = appVersion;
        this.zitadelIdentityProviderService = zitadelIdentityProviderService;
        this.pluginManager = pluginManager;
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
        File apiFile =
                new File(
                        this.openApiGeneratorJarManage.getOpenApiWorkdir(),
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
                new File(
                        this.openApiGeneratorJarManage.getOpenApiWorkdir(),
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
        File file =
                new File(
                        this.openApiGeneratorJarManage.getOpenApiWorkdir(),
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
                String comm =
                        String.format(
                                "java -jar %s generate -g html2 " + "-i %s -o %s",
                                jarPath.getPath(), yamlFile.getPath(), openApiDir);
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
                    log.error("Create service openApi html file failed. {}", stdErrOut);
                }
                // Modify the file name to serviceId.html
                File tempHtmlFile = new File(openApiDir, "index.html");
                if (tempHtmlFile.exists() && (tempHtmlFile.renameTo(htmlFile))) {
                    log.info(
                            "Created service openApi html file:{} successfully.",
                            htmlFile.getName());
                    if (htmlFile.exists()) {
                        return this.openApiUrlManage.getOpenApiUrl(serviceId);
                    }
                }
            } else {
                log.error(
                        "Not generating {} file. Missing json or openapi-generator jar file",
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
                    log.info("Deleted temp yaml file:{} successfully.", yamlFile.getName());
                } catch (IOException ioException) {
                    log.info("Deleting temp yaml file:{} failed.", yamlFile.getName(), ioException);
                }
            }
        }
    }

    /**
     * Get the path of the openapi-generator-cli.jar used.
     *
     * @return File The openapi-generator-cli.jar path.
     */
    private File getJarPath() throws IOException {
        return this.openApiGeneratorJarManage.getCliFile();
    }

    private String getApiDocsJson(ServiceTemplateEntity registerService) {
        if (Objects.isNull(registerService)) {
            return StringUtils.EMPTY;
        }
        // version of registered service.
        String serviceVersion = registerService.getVersion();
        // service url.
        String serviceUrl = this.openApiUrlManage.getServiceUrl();
        // security of service.
        String securityConfigList = getSecurityConfigList();
        // required roles in description of the API.
        String requiredRolesDesc = getRequiredRolesDesc();
        // string of required fields list.
        String createRequiredStr = null;
        // category value of registered service.
        String category = registerService.getCategory().toValue();
        // string of category values list.
        String categoryValuesStr = null;
        // name of registered service.
        String serviceName = registerService.getName();
        // example value of name of region.
        String regionNameExample = null;
        // list all values of names of all regions.
        String regionNamesStr = null;
        // example value of site of region.
        String regionSiteExample = null;
        // list all values of sites of all regions.
        String regionSitesStr = null;
        // example value of area of region.
        String regionAreaExample = null;
        // list all values of areas of all regions.
        String regionAreasStr = null;
        // csp value of registered service.
        String csp = registerService.getCsp().toValue();
        // string of csp values list.
        String cspValuesStr = null;
        // example value of flavor.
        String flavorNameExample = null;
        // list all values of all flavors.
        String flavorNamesStr = null;
        // serviceHostingType value of registered service.
        String serviceHostingType = registerService.getServiceHostingType().toValue();
        // string of csp serviceHostingType list.
        String serviceHostingTypesStr = null;
        // properties for deploy service
        String propertiesStr = null;
        // string of required properties.
        String propertiesRequiredStr = null;
        // schema of availabilityZones.
        String availabilityZonesSchemaStr = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            createRequiredStr = mapper.writeValueAsString(getRequiredFields(new DeployRequest()));
            propertiesStr =
                    objectMapper.writeValueAsString(
                            convertJsonSchemaSpecToOpenApiSpec(
                                    registerService.getJsonObjectSchema().getProperties()));
            propertiesRequiredStr =
                    objectMapper.writeValueAsString(
                            registerService.getJsonObjectSchema().getRequired());
            List<Region> regions = registerService.getOcl().getCloudServiceProvider().getRegions();

            Region regionExample = regions.getFirst();
            regionNameExample = regionExample.getName();
            regionNamesStr =
                    mapper.writeValueAsString(regions.stream().map(Region::getName).toList());
            regionSiteExample = regionExample.getSite();
            regionSitesStr =
                    mapper.writeValueAsString(regions.stream().map(Region::getSite).toList());
            regionAreaExample = regionExample.getArea();
            regionAreasStr =
                    mapper.writeValueAsString(regions.stream().map(Region::getArea).toList());
            List<ServiceFlavorWithPrice> flavors =
                    registerService.getOcl().getFlavors().getServiceFlavors();
            flavorNameExample = flavors.getFirst().getName();
            flavorNamesStr =
                    mapper.writeValueAsString(
                            flavors.stream().map(ServiceFlavor::getName).toList());
            cspValuesStr = mapper.writeValueAsString(getActiveCspValues());
            categoryValuesStr = mapper.writeValueAsString(getCategoryValues());
            serviceHostingTypesStr = mapper.writeValueAsString(getServiceHostingTypeValues());
            availabilityZonesSchemaStr =
                    getSchemaOfAvailabilityZones(
                            registerService.getOcl().getDeployment().getServiceAvailabilityConfig(),
                            mapper);
        } catch (JsonProcessingException e) {
            log.error("Failed to write value as string.", e);
        }
        // CHECKSTYLE OFF: LineLength
        return String.format(
                """
{
    "openapi": "3.0.1",
    "info": {
        "title": "OpenAPI definition",
        "version": "%s",
        "description": "OpenAPI for creating an order task to deploy new service using approved service template."
    },
    "servers": [
        {
            "url": "%s",
            "description": "Generated server url"
        }
    ],
    %s
    "tags": [
        {
            "name": "Service",
            "description": "APIs to manage the service instances"
        }
    ],
    "paths": {
        "/xpanse/services": {
            "post": {
                "tags": [
                    "Service"
                ],
                "description": "Create an order task to deploy new service using approved service template.%s",
                "operationId": "deploy",
                "requestBody": {
                    "content": {
                        "application/json": {
                            "schema": {
                                "$ref": "#/components/schemas/DeployRequest"
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
                                    "$ref": "#/components/schemas/ServiceOrder"
                                }
                            }
                        }
                    },
                    "400": {
                        "description": "Bad Request",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/OrderFailedResponse"
                                }
                            }
                        }
                    },
                    "401": {
                        "description": "Unauthorized",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/OrderFailedResponse"
                                }
                            }
                        }
                    },
                    "403": {
                        "description": "Forbidden",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/OrderFailedResponse"
                                }
                            }
                        }
                    },
                    "422": {
                        "description": "Unprocessable Entity",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/OrderFailedResponse"
                                }
                            }
                        }
                    },
                    "500": {
                        "description": "Internal Server Error",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/OrderFailedResponse"
                                }
                            }
                        }
                    },
                    "502": {
                        "description": "Bad Gateway",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/OrderFailedResponse"
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
            "OrderFailedResponse": {
                "required": ["details", "errorType", "success"],
                "type": "object",
                "properties": {
                    "errorType": {
                        "type": "string",
                        "description": "The result code of response.",
                        "enum": [
                            "Success",
                            "Runtime Error",
                            "Parameters Invalid",
                            "Terraform Script Invalid",
                            "Unprocessable Entity",
                            "Response Not Valid",
                            "Credentials Not Found",
                            "Flavor Invalid",
                            "Terraform Execution Failed",
                            "Plugin Not Found",
                            "Deployer Not Found",
                            "Unhandled Exception",
                            "Unavailable Service Regions",
                            "Service Deployment Not Found",
                            "Deployment Variable Invalid",
                            "Unauthorized",
                            "Access Denied",
                            "Sensitive Field Encryption Or Decryption Failed Exception",
                            "Unsupported Enum Value",
                            "Terra Boot Request Failed",
                            "Tofu Maker Request Failed",
                            "Variable Validation Failed",
                            "Variable Schema Definition Invalid",
                            "Policy Evaluation Failed",
                            "Current Login User No Found",
                            "Eula Not Accepted",
                            "Service Flavor Downgrade Not Allowed",
                            "Service Order Not Found",
                            "Service Price Calculation Failed",
                            "Invalid Git Repo Details",
                            "File Locked",
                            "Service Configuration Invalid"
                          ]
                      },
                      "details": {
                          "type": "array",
                          "description": "Details of the errors occurred",
                          "items": {
                              "type": "string",
                              "description": "Details of the errors occurred"
                          }
                      },
                      "success": {
                          "type": "boolean",
                          "description": "Describes if the request is successful"
                      },
                      "serviceId": {
                          "type": "string",
                          "description": "The service id associated with the request."
                      },
                      "orderId": {
                          "type": "string",
                          "description": "The order id associated with the request."
                      }
                  }
              },
              "ServiceOrder": {
                  "required": ["orderId", "serviceId"],
                  "type": "object",
                  "properties": {
                      "orderId": {
                          "type": "string",
                          "description": "The id of the service order.",
                          "format": "uuid"
                      },
                      "serviceId": {
                          "type": "string",
                          "description": "The id of the deployed service.",
                          "format": "uuid"
                      }
                  }
              },
            "DeployRequest": {
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
                       "required": [
                           "area",
                           "name",
                           "site"
                       ],
                       "type": "object",
                       "properties": {
                           "name": {
                               "example": "%s",
                               "type": "string",
                               "description": "The name of the Region",
                               "enum": %s
                           },
                           "site": {
                               "example": "%s",
                               "type": "string",
                               "description": "The site which the region belongs to, such as default, International, Chinese Mainland",
                               "enum": %s
                           },
                           "area": {
                               "example": "%s",
                               "type": "string",
                               "description": "The area which the region belongs to, such as Asia, Europe, Africa",
                               "enum": %s
                           }
                       },
                       "description": "The regions of the Cloud Service Provider"
                    },
                    "csp": {
                        "default": "%s",
                        "type": "string",
                        "description": "The csp of the Service.",
                        "enum": %s
                    },
                    "flavor": {
                        "example": "%s",
                        "type": "string",
                        "description": "The flavor of the Service.",
                        "enum": %s
                    },
                    "serviceHostingType": {
                        "default": "%s",
                        "type": "string",
                        "description": "Defines which cloud service account is used for deploying cloud resources.",
                        "enum": %s
                    },
                    "serviceRequestProperties": {
                         "required": %s,
                         "description": "The variables to deploy the service instance.",
                         "type": "object",
                         "properties": %s
                    }%s                          \s
                }
            }                                                       \s
        }%s                                     \s
    }
}
""",
                appVersion,
                serviceUrl,
                securityConfigList,
                requiredRolesDesc,
                createRequiredStr,
                category,
                categoryValuesStr,
                serviceName,
                serviceVersion,
                regionNameExample,
                regionNamesStr,
                regionSiteExample,
                regionSitesStr,
                regionAreaExample,
                regionAreasStr,
                csp,
                cspValuesStr,
                flavorNameExample,
                flavorNamesStr,
                serviceHostingType,
                serviceHostingTypesStr,
                propertiesRequiredStr,
                propertiesStr,
                availabilityZonesSchemaStr,
                getSecuritySchemes());
    }

    private String getSecurityConfigList() {
        if (webSecurityIsEnabled) {
            String roleScopeStr =
                    roleProtectionIsEnabled ? "\"urn:zitadel:iam:org:project:roles\"," : "";
            return String.format(
                    """
                    "security": [
                                {
                                    "OAuth2Flow": [
                                        "openid",
                                        "profile",
                                        %s
                                        "urn:zitadel:iam:user:metadata"
                                    ]
                                }
                            ],
                    """,
                    roleScopeStr);
        }
        return "";
    }

    private String getRequiredRolesDesc() {
        if (webSecurityIsEnabled && roleProtectionIsEnabled) {
            return "<br>Required role:<b> admin</b> or <b>user</b>";
        }
        return "";
    }

    private String getSecuritySchemes() {
        if (Objects.nonNull(zitadelIdentityProviderService)) {
            // CHECKSTYLE OFF: Indentation
            return """
,
"securitySchemes": {
    "OAuth2Flow": {
        "type": "oauth2",
        "flows": {
            "authorizationCode": {
                "authorizationUrl": "https://iam.xpanse.site/oauth/v2/authorize",
                "tokenUrl": "https://iam.xpanse.site/oauth/v2/token",
                "scopes": {
                    "openid": "mandatory must be selected.",
                    "profile": "mandatory must be selected.",
                    "urn:zitadel:iam:org:project:roles": "mandatory must be selected.",
                    "urn:zitadel:iam:user:metadata": "mandatory must be selected."
                }
            }
        }
    }
}
""";
            // CHECKSTYLE ON: Indentation
        }
        return "";
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

    private List<String> getActiveCspValues() {
        return pluginManager.getPluginsMap().keySet().stream()
                .map(Csp::toValue)
                .collect(Collectors.toList());
    }

    private List<String> getCategoryValues() {
        return Arrays.stream(Category.values()).map(Category::toValue).collect(Collectors.toList());
    }

    private List<String> getServiceHostingTypeValues() {
        return Arrays.stream(ServiceHostingType.values())
                .map(ServiceHostingType::toValue)
                .collect(Collectors.toList());
    }

    private Map<String, Map<String, Object>> convertJsonSchemaSpecToOpenApiSpec(
            Map<String, Map<String, Object>> properties) {
        properties.forEach(
                (key, value) -> {
                    String exampleValue = (String) value.get(JSON_SCHEMA_DEF_EXAMPLE_KEYWORD);
                    if (Objects.nonNull(exampleValue)) {
                        value.remove(JSON_SCHEMA_DEF_EXAMPLE_KEYWORD);
                        value.put(OPENAPI_EXAMPLE_KEYWORD, exampleValue);
                    }
                });
        return properties;
    }

    private String getSchemaOfAvailabilityZones(
            List<AvailabilityZoneConfig> availabilityZones, ObjectMapper mapper) {
        String availabilityZonesSchemaStr = "";
        try {
            if (!CollectionUtils.isEmpty(availabilityZones)) {
                availabilityZonesSchemaStr =
                        String.format(
                                """
                   ,
                   "availabilityZones": {
                       "required": %s,
                       "description": "The availability zones to deploy the service instance.",
                       "type": "object",
                       "properties": %s
                   }
""",
                                mapper.writeValueAsString(
                                        getRequiredAvailabilityZones(availabilityZones)),
                                mapper.writeValueAsString(
                                        convertAvailabilityZonesToOpenApiSpec(availabilityZones)));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to get schema of availability zones.", e);
        }
        return availabilityZonesSchemaStr;
    }

    private List<String> getRequiredAvailabilityZones(
            List<AvailabilityZoneConfig> availabilityZones) {
        return availabilityZones.stream()
                .filter(AvailabilityZoneConfig::getMandatory)
                .map(AvailabilityZoneConfig::getVarName)
                .toList();
    }

    private Map<String, Map<String, Object>> convertAvailabilityZonesToOpenApiSpec(
            List<AvailabilityZoneConfig> availabilityZones) {
        Map<String, Map<String, Object>> availabilityZonesExample = new HashMap<>();
        for (AvailabilityZoneConfig availabilityZone : availabilityZones) {
            Map<String, Object> zoneConfigMap = new HashMap<>();
            zoneConfigMap.put(OPENAPI_TYPE_KEYWORD, "string");
            zoneConfigMap.put(OPENAPI_EXAMPLE_KEYWORD, OPENAPI_EXAMPLE_KEYWORD);
            if (StringUtils.isNotBlank(availabilityZone.getDescription())) {
                zoneConfigMap.put(OPENAPI_DESCRIPTION_KEYWORD, availabilityZone.getDescription());
            }
            availabilityZonesExample.put(availabilityZone.getVarName(), zoneConfigMap);
        }
        return availabilityZonesExample;
    }
}
