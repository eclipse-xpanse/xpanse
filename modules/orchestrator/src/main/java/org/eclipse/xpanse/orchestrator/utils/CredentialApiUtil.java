/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Class for credentialApi generation.
 */
@Slf4j
@Component
public class CredentialApiUtil implements ApplicationRunner {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final String appVersion;
    private final OpenApiUtil openApiUtil;
    private final OrchestratorService orchestratorService;

    /**
     * Constructor of CredentialApiUtil.
     */
    @Autowired
    public CredentialApiUtil(OrchestratorService orchestratorService,
                             OpenApiUtil openApiUtil,
                             @Value("${app.version:1.0.0}")
                                     String appVersion) {
        this.orchestratorService = orchestratorService;
        this.openApiUtil = openApiUtil;
        this.appVersion = appVersion;
    }

    /**
     * Get serviceUrl.
     *
     * @return serviceUrl
     */
    public String getServiceUrl() {
        return openApiUtil.getServiceUrl();
    }

    /**
     * Get the work directory of the credentialApi.
     *
     * @return workdir  The work directory of the credentialApi.
     */
    private String getCredentialApiDir() {
        return openApiUtil.getOpenApiWorkdir();
    }


    @Override
    public void run(ApplicationArguments args) {
        Map<Csp, OrchestratorPlugin> cspOrchestratorPluginMap = orchestratorService.pluginMap();
        for (Csp csp : cspOrchestratorPluginMap.keySet()) {
            OrchestratorPlugin orchestratorPlugin = cspOrchestratorPluginMap.get(csp);
            List<AbstractCredentialInfo> credentialDefinitions =
                    orchestratorPlugin.getCredentialDefinitions();
            if (Objects.nonNull(credentialDefinitions)) {
                Map<CredentialType, AbstractCredentialInfo> typeCredentialInfoMap =
                        credentialDefinitions.stream().filter(Objects::nonNull)
                                .collect(Collectors.toMap(AbstractCredentialInfo::getType,
                                        Function.identity(), (existing, replacement) -> existing));
                typeCredentialInfoMap.keySet().forEach(type -> {
                    CredentialDefinition credentialDefinition =
                            (CredentialDefinition) typeCredentialInfoMap.get(type);
                    createCredentialApi(credentialDefinition);
                });
            } else {
                log.info("Not found credential definition of the cloud service provider:{}",
                        csp.toValue());
            }
        }
    }

    /**
     * create credentialApi for plugins.
     *
     * @param credentialDefinition credentialDefinition
     */
    public void createCredentialApi(CredentialDefinition credentialDefinition) {
        String jsonFileName = getCredentialApiFileName(credentialDefinition.getCsp(),
                credentialDefinition.getType(), ".json");
        String htmlFileName = getCredentialApiFileName(credentialDefinition.getCsp(),
                credentialDefinition.getType(), ".html");
        String credentialApiDir = getCredentialApiDir();
        File dir = new File(credentialApiDir);
        if (!dir.exists()) {
            log.info("Create service openApi dir:{} successfully.", credentialApiDir);
        }
        File jsonFile = new File(credentialApiDir, jsonFileName);
        File htmlFile = new File(credentialApiDir, htmlFileName);
        htmlFile.deleteOnExit();
        try {
            String apiDocsJson = getApiDocsJson(credentialDefinition);
            try (FileWriter apiWriter = new FileWriter(jsonFile.getPath())) {
                apiWriter.write(apiDocsJson);
            }
            log.info("credentialApi jsonFile:{} creation successful.", jsonFile.getName());
            if (jsonFile.exists() && openApiUtil.downloadClientJar(credentialApiDir)) {
                File jarPath = new File(credentialApiDir, "openapi-generator-cli.jar");
                String comm = String.format("java -jar %s generate -g html2 "
                        + "-i %s -o %s", jarPath.getPath(), jsonFile.getPath(), credentialApiDir);
                ProcessBuilder processBuilder = new ProcessBuilder(comm.split("\\s+"));
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                StringBuilder stdErrOut = new StringBuilder();
                try (BufferedReader outputReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        stdErrOut.append(line);
                    }
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.error("credentialApi htmlFile:{} creation failed.{}",
                            htmlFile.getName(), stdErrOut);
                }
                File tempHtmlFile = new File(credentialApiDir, "index.html");
                if (tempHtmlFile.exists()) {
                    if (tempHtmlFile.renameTo(htmlFile)) {
                        log.info("credentialApi htmlFile:{} creation successful.",
                                htmlFile.getName());
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            log.error("credentialApi html file:{} creation failed.", htmlFile.getName(), ex);
            throw new RuntimeException("credentialApi html file creation failed.", ex);
        } finally {
            // Delete the json file named ${Csp}_credentialApi.yaml
            if (jsonFile.exists()) {
                if (jsonFile.delete()) {
                    log.info("Delete temp json file:{} successfully.", jsonFile.getName());
                }
            }
        }
    }

    /**
     * Get credential openApi Url.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns credential openApi Url.
     */
    public String getCredentialOpenApiUrl(Csp csp, CredentialType type) {
        String htmlFileName = getCredentialApiFileName(csp, type, ".html");
        String credentialApiDir = getCredentialApiDir();
        File htmlFile = new File(credentialApiDir, htmlFileName);
        if (!htmlFile.exists()) {
            boolean findCredentialInfo = false;
            OrchestratorPlugin orchestratorPlugin = orchestratorService.getOrchestratorPlugin(csp);
            List<AbstractCredentialInfo> credentialDefinitions =
                    orchestratorPlugin.getCredentialDefinitions();
            if (Objects.nonNull(credentialDefinitions)) {
                AbstractCredentialInfo abstractCredentialInfo = credentialDefinitions.stream()
                        .filter(credentialInfo -> Objects.equals(type, credentialInfo.getType()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(abstractCredentialInfo)) {
                    findCredentialInfo = true;
                    createCredentialApi((CredentialDefinition) abstractCredentialInfo);
                }
            }
            if (!findCredentialInfo) {
                String errorMsg = String.format(
                        "Not found credential definition with type %s of the cloud service "
                                + "provider %s", type.toValue(), csp.toValue());
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
        if (openApiUtil.getOpenapiPath().endsWith("/")) {
            return getServiceUrl() + "/" + openApiUtil.getOpenapiPath() + htmlFileName;
        }
        return getServiceUrl() + "/" + openApiUtil.getOpenapiPath() + "/" + htmlFileName;
    }

    private String getCredentialApiFileName(Csp csp, CredentialType type, String suffix) {
        return csp.toValue() + "_" + type.toValue() + "_credentialApi" + suffix;
    }

    private String getVariablesExampleStr(List<CredentialVariable> variables) {
        variables.forEach(credentialVariable -> credentialVariable.setValue(
                credentialVariable.getDescription()));
        String exampleString = "";
        try {
            exampleString = mapper.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            log.error("Failed to write value as string.", e);
        }
        return exampleString;
    }

    private String getApiDocsJson(CredentialDefinition credentialDefinition) {
        String serviceUrl = getServiceUrl();
        String csp = credentialDefinition.getCsp().toValue();
        String type = credentialDefinition.getType().toValue();
        String variablesExampleStr = getVariablesExampleStr(credentialDefinition.getVariables());
        //CHECKSTYLE OFF: LineLength
        return String.format("""
                        {
                            "openapi": "3.0.1",
                            "info": {
                                "title": "OpenAPI definition",
                                "description": "OpenAPI for user adding credential of the cloud service provider %s",
                                "version": "%s"
                            },
                            "servers": [
                                {
                                    "url": "%s",
                                    "description": "Generated server url"
                                }
                            ],
                            "tags": [
                                {
                                    "name": "Credentials Management",
                                    "description": "APIs to manage credentials for authentication."
                                }
                            ],
                            "paths": {
                                "/xpanse/auth/csp/%s/credential": {
                                    "post": {
                                        "tags": [
                                            "Credentials Management"
                                        ],
                                        "description": "Add credential with type %s of the cloud service provider %s.",
                                        "operationId": "addCredential",
                                        "requestBody": {
                                            "content": {
                                                "application/json": {
                                                    "schema": {
                                                        "$ref": "#/components/schemas/CreateCredential"
                                                    }
                                                }
                                            },
                                            "required": true
                                        },
                                        "responses": {
                                            "200": {
                                                "description": "OK",
                                                "content": {
                                                    "application/json": {
                                                        "schema": {
                                                            "type": "boolean"
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
                                            "422": {
                                                "description": "Unprocessable Entity",
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
                                            "details",
                                            "resultType",
                                            "success"
                                        ],
                                        "type": "object",
                                        "properties": {
                                            "resultType": {
                                                "type": "string",
                                                "description": "The result code of response.",
                                                "enum": [
                                                    "Success",
                                                    "Runtime Failure",
                                                    "Parameters Invalid",
                                                    "Terraform Script Invalid",
                                                    "Unprocessable Entity",
                                                    "Response Not Valid"
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
                                            }
                                        }
                                    },
                                    "CreateCredential": {
                                        "required": [
                                            "csp",
                                            "name",
                                            "timeToLive",
                                            "type",
                                            "userName",
                                            "variables"
                                        ],
                                        "type": "object",
                                        "properties": {
                                            "name": {
                                                "type": "string",
                                                "example": "%s",
                                                "description": "The name of the credential"
                                            },
                                            "userName": {
                                                "type": "string",
                                                "example": "%s",
                                                "description": "User who create the credential."
                                            },
                                            "csp": {
                                                "type": "string",
                                                "example": "%s",
                                                "description": "The cloud service provider of the credential.",
                                                "enum": [
                                                    "aws",
                                                    "azure",
                                                    "alicloud",
                                                    "huawei",
                                                    "openstack",
                                                    "flexibleEngine"
                                                ]
                                            },
                                            "description": {
                                                "type": "string",
                                                "example": "%s",
                                                "description": "The description of the credential"
                                            },
                                            "type": {
                                                "type": "string",
                                                "example": "%s",
                                                "description": "The type of the credential",
                                                "enum": [
                                                    "variables",
                                                    "http_authentication",
                                                    "api_key",
                                                    "oauth2"
                                                ]
                                            },
                                            "variables": {
                                                "type": "array",
                                                "example": %s,
                                                "description": "The variables list of the credential",
                                                "items": {
                                                    "$ref": "#/components/schemas/CredentialVariable"
                                                }
                                            },
                                            "timeToLive": {
                                                "type": "integer",
                                                "description": "The time in seconds to live of the credential",
                                                "format": "int32",
                                                "example": 3600
                                            }
                                        }
                                    },
                                    "CredentialVariable": {
                                        "required": [
                                            "description",
                                            "name",
                                            "value"
                                        ],
                                        "type": "object",
                                        "properties": {
                                            "name": {
                                                "type": "string",
                                                "description": "The name of the CredentialVariable,this field is provided by the the plugin of cloud service provider."
                                            },
                                            "description": {
                                                "type": "string",
                                                "description": "The description of the CredentialVariable,this field is provided by the plugin of cloud service provider."
                                            },
                                            "value": {
                                                "type": "string",
                                                "description": "The value of the CredentialVariable, this field is filled by the user."
                                            }
                                        },
                                        "description": "The variables list of the credential"
                                    }
                                }
                            }
                        }
                        """,
                csp, appVersion, serviceUrl, csp, type, csp, credentialDefinition.getName(),
                credentialDefinition.getXpanseUser(), csp, credentialDefinition.getDescription(),
                type, variablesExampleStr);
    }
}
