/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.NoCredentialDefinitionAvailable;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.auth.zitadel.ZitadelIdentityProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Class for credentialApi generation. */
@Slf4j
@Component
public class CredentialOpenApiGenerator implements ApplicationListener<ApplicationStartedEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String appVersion;
    private final OpenApiUrlManage openApiUrlManage;
    private final PluginManager pluginManager;
    private final OpenApiGeneratorJarManage openApiGeneratorJarManage;
    private final ZitadelIdentityProviderService zitadelIdentityProviderService;

    @Value("${enable.web.security:false}")
    private Boolean webSecurityIsEnabled;

    @Value("${enable.role.protection:false}")
    private Boolean roleProtectionIsEnabled;

    /** Constructor of CredentialOpenApiGenerator. */
    @Autowired
    public CredentialOpenApiGenerator(
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
     * Get serviceUrl.
     *
     * @return serviceUrl
     */
    public String getServiceUrl() {
        return openApiUrlManage.getServiceUrl();
    }

    /**
     * Get the work directory of the credentialApi.
     *
     * @return workdir The work directory of the credentialApi.
     */
    private String getCredentialApiDir() {
        return this.openApiGeneratorJarManage.getOpenApiWorkdir();
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        Map<Csp, OrchestratorPlugin> cspOrchestratorPluginMap = pluginManager.getPluginsMap();
        for (Map.Entry<Csp, OrchestratorPlugin> entry : cspOrchestratorPluginMap.entrySet()) {
            List<AbstractCredentialInfo> credentialDefinitions =
                    entry.getValue().getCredentialDefinitions();
            if (Objects.nonNull(credentialDefinitions)) {
                Map<CredentialType, AbstractCredentialInfo> typeCredentialInfoMap =
                        credentialDefinitions.stream()
                                .filter(Objects::nonNull)
                                .collect(
                                        Collectors.toMap(
                                                AbstractCredentialInfo::getType,
                                                Function.identity(),
                                                (existing, replacement) -> existing));
                typeCredentialInfoMap
                        .keySet()
                        .forEach(
                                type -> {
                                    CredentialVariables credentialVariables =
                                            (CredentialVariables) typeCredentialInfoMap.get(type);
                                    createCredentialApi(credentialVariables);
                                });
            } else {
                log.info(
                        "Not found credential definition of the cloud service provider:{}",
                        entry.getKey().toValue());
            }
        }
    }

    /**
     * create credentialApi for plugins.
     *
     * @param credentialVariables credentialDefinition
     */
    public void createCredentialApi(CredentialVariables credentialVariables) {
        String jsonFileName =
                getCredentialApiFileName(
                        credentialVariables.getCsp(), credentialVariables.getType(), ".json");
        String htmlFileName =
                getCredentialApiFileName(
                        credentialVariables.getCsp(), credentialVariables.getType(), ".html");
        String credentialApiDir = getCredentialApiDir();
        File dir = new File(credentialApiDir);
        if (!dir.exists()) {
            log.info("Create service openApi dir:{} successfully.", credentialApiDir);
        }
        File jsonFile = new File(credentialApiDir, jsonFileName);
        File htmlFile = new File(credentialApiDir, htmlFileName);
        htmlFile.deleteOnExit();
        try {
            String apiDocsJson = getApiDocsJson(credentialVariables);
            try (FileWriter apiWriter = new FileWriter(jsonFile.getPath())) {
                apiWriter.write(apiDocsJson);
            }
            log.info("credentialApi jsonFile:{} creation successful.", jsonFile.getName());
            File jarPath = getJarPath();
            if (jsonFile.exists() && jarPath.exists()) {
                String comm =
                        String.format(
                                "java -jar %s generate -g html2 " + "-i %s -o %s",
                                jarPath.getPath(), jsonFile.getPath(), credentialApiDir);
                ProcessBuilder processBuilder = new ProcessBuilder(comm.split("\\s+"));
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                StringBuilder stdErrOut = new StringBuilder();
                try (BufferedReader outputReader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        stdErrOut.append(line);
                    }
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.error(
                            "credentialApi htmlFile:{} creation failed.{}",
                            htmlFile.getName(),
                            stdErrOut);
                }
                File tempHtmlFile = new File(credentialApiDir, "index.html");
                if (tempHtmlFile.exists() && (tempHtmlFile.renameTo(htmlFile))) {
                    log.info("credentialApi htmlFile:{} creation successful.", htmlFile.getName());
                }
            } else {
                log.error(
                        "Not generating {} file. Missing json or openapi-generator jar file",
                        htmlFile.getName());
            }
        } catch (IOException | InterruptedException ex) {
            log.error("credentialApi html file:{} creation failed.", htmlFile.getName(), ex);
        } finally {
            // Delete the json file named ${Csp}_credentialApi.yaml
            if (jsonFile.exists()) {
                try {
                    Files.delete(jsonFile.toPath());
                    log.info("Deleted temp json file:{} successfully.", jsonFile.getName());
                } catch (IOException ioException) {
                    log.info("Deleting temp json file:{} failed.", jsonFile.getName(), ioException);
                }
            }
        }
    }

    /**
     * Get the path of the openapi-generator-cli.jar used.
     *
     * @return File The openapi-generator-cli.jar path.
     */
    private File getJarPath() {
        return this.openApiGeneratorJarManage.getCliFile();
    }

    /**
     * Get credential openApi Url.
     *
     * @param csp The cloud service provider.
     * @param type The type of credential.
     * @return Returns credential openApi Url.
     */
    public String getCredentialOpenApiUrl(Csp csp, CredentialType type) {
        String htmlFileName = getCredentialApiFileName(csp, type, ".html");
        String credentialApiDir = getCredentialApiDir();
        File htmlFile = new File(credentialApiDir, htmlFileName);
        if (!htmlFile.exists()) {
            boolean findCredentialInfo = false;
            OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
            List<AbstractCredentialInfo> credentialDefinitions =
                    orchestratorPlugin.getCredentialDefinitions();
            if (Objects.nonNull(credentialDefinitions)) {
                AbstractCredentialInfo abstractCredentialInfo =
                        credentialDefinitions.stream()
                                .filter(
                                        credentialInfo ->
                                                Objects.equals(type, credentialInfo.getType()))
                                .findFirst()
                                .orElse(null);
                if (Objects.nonNull(abstractCredentialInfo)) {
                    findCredentialInfo = true;
                    createCredentialApi((CredentialVariables) abstractCredentialInfo);
                }
            }
            if (!findCredentialInfo) {
                String errorMsg =
                        String.format(
                                "Not found credential definition with type %s of the cloud service "
                                        + "provider %s",
                                type.toValue(), csp.toValue());
                log.error(errorMsg);
                throw new NoCredentialDefinitionAvailable(errorMsg);
            }
        }
        if (openApiGeneratorJarManage.getOpenapiPath().endsWith("/")) {
            return getServiceUrl()
                    + "/"
                    + openApiGeneratorJarManage.getOpenapiPath()
                    + htmlFileName;
        }
        return getServiceUrl()
                + "/"
                + openApiGeneratorJarManage.getOpenapiPath()
                + "/"
                + htmlFileName;
    }

    private String getCredentialApiFileName(Csp csp, CredentialType type, String suffix) {
        return csp.toValue() + "_" + type.toValue() + "_credentialApi" + suffix;
    }

    private String getVariablesExampleStr(List<CredentialVariable> variables) {
        variables.forEach(
                credentialVariable ->
                        credentialVariable.setValue(credentialVariable.getDescription()));
        String exampleString = "";
        try {
            exampleString = OBJECT_MAPPER.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            log.error("Failed to write value as string.", e);
        }
        return exampleString;
    }

    private List<String> getActiveCspValues() {
        return pluginManager.getPluginsMap().keySet().stream()
                .map(Csp::toValue)
                .collect(Collectors.toList());
    }

    private String getApiDocsJson(CredentialVariables credentialVariables)
            throws JsonProcessingException {
        String serviceUrl = getServiceUrl();
        String cspValuesStr = OBJECT_MAPPER.writeValueAsString(getActiveCspValues());
        String securityConfigList = getSecurityConfigList();
        String csp = credentialVariables.getCsp().toValue();
        List<String> siteValues =
                pluginManager.getOrchestratorPlugin(credentialVariables.getCsp()).getSites();
        String siteValuesStr = OBJECT_MAPPER.writeValueAsString(siteValues);
        String siteExample = siteValues.isEmpty() ? "default" : siteValues.getFirst();
        String type = credentialVariables.getType().toValue();
        String variablesExampleStr = getVariablesExampleStr(credentialVariables.getVariables());
        // CHECKSTYLE OFF: LineLength
        return String.format(
                """
{
    "openapi": "3.0.1",
    "info": {
        "title": "OpenAPI definition",
        "description": "OpenAPI for users adding credential to connect the cloud service provider %s",
        "version": "%s"
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
            "name": "User Credentials Management",
            "description": "APIs to manage user's credentials for authentication."
        }
    ],
    "paths": {
        "/xpanse/user/credentials": {
            "post": {
                "tags": [
                    "User Credentials Management"
                ],
                "description": "Add user's credential for connecting to the cloud service provider.%s",
                "operationId": "addUserCredential",
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
                    "204": {
                        "description": "No Content"
                    },
                    "400": {
                        "description": "Bad Request",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/Response"
                                }
                            }
                        }
                    },
                    "401": {
                        "description": "Unauthorized",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/Response"
                                }
                            }
                        }
                    },
                    "403": {
                        "description": "Forbidden",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/Response"
                                }
                            }
                        }
                    },
                    "422": {
                        "description": "Unprocessable Entity",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/Response"
                                }
                            }
                        }
                    },
                    "500": {
                        "description": "Internal Server Error",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/Response"
                                }
                            }
                        }
                    },
                    "502": {
                        "description": "Bad Gateway",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/Response"
                                }
                            }
                        }
                    }                                       \s
                }
            }
        }
    },
    "components": {
        "schemas": {
            "Response": {
                "required": [
                    "details",
                    "errorType",
                    "success"
                ],
                "type": "object",
                "properties": {
                    "errorType": {
                        "type": "string",
                        "description": "The result code of response.",
                        "enum": [
                            "Success",
                            "Runtime Failure",
                            "Parameters Invalid",
                            "Credential Capability Not Found",
                            "Credentials Not Found",
                            "Credential Variables Not Complete",
                            "Unprocessable Entity",
                            "Response Not Valid",
                            "No Credential Definition Available",
                            "Unhandled Exception",
                            "Unauthorized",
                            "Access Denied",
                            "Unsupported Enum Value",
                            "Current Login User No Found"
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
                    "site",
                    "name",
                    "timeToLive",
                    "type",
                    "variables"
                ],
                "type": "object",
                "properties": {
                    "csp": {
                        "type": "string",
                        "example": "%s",
                        "description": "The cloud service provider of the credential.",
                        "enum": %s
                    },
                    "site": {
                        "type": "string",
                        "example": "%s",
                        "description": "The site to which the credentials belong to.",
                        "enum": %s
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
                    "name": {
                        "type": "string",
                        "example": "%s",
                        "description": "The name of the credential"
                    },
                    "description": {
                        "type": "string",
                        "example": "%s",
                        "description": "The description of the credential"
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
                "required": ["description", "isSensitive", "name", "value"],
                "type": "object",
                "properties": {
                    "name": {
                        "type": "string",
                        "description": "The name of the CredentialVariable,this field is provided by the plugin of cloud service provider."
                    },
                    "description": {
                        "type": "string",
                        "description": "The description of the CredentialVariable,this field is provided by the plugin of cloud service provider."
                    },
                    "isMandatory": {
                        "type": "boolean",
                        "description": "If the variable is mandatory. If is optional then the credential completeness check will ignore this variable. It is upto the plugin to decide what needs to be done if this optional credential variable is present.",
                        "default": true
                    },
                    "isSensitive": {
                        "type": "boolean",
                        "example": true,
                        "description": "Defines if the particular variable contains sensitive data. For example the value is false for username and true for password variables respectively."
                    },
                    "value": {
                        "type": "string",
                        "description": "The value of the CredentialVariable, this field is filled by the user."
                    }
                },
                "description": "The variables list of the credential. The list elements must be unique."
            }
        }%s
    }
}
""",
                csp,
                appVersion,
                serviceUrl,
                securityConfigList,
                getRequiredRolesDesc(),
                csp,
                cspValuesStr,
                siteExample,
                siteValuesStr,
                type,
                credentialVariables.getName(),
                credentialVariables.getDescription(),
                variablesExampleStr,
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
}
