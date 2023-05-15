/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.slf4j.MDC;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Class for credentialApi generation.
 */
@Slf4j
@Component
public class CredentialApiUtil implements ApplicationRunner {

    private static final String TASK_ID = "TASK_ID";

    @Resource
    OpenApiUtil openApiUtil;
    @Resource
    OrchestratorService orchestratorService;

    @Override
    public void run(ApplicationArguments args) {
        Map<Csp, OrchestratorPlugin> cspOrchestratorPluginMap = orchestratorService.pluginMap();
        cspOrchestratorPluginMap.keySet().forEach(csp -> {
            OrchestratorPlugin orchestratorPlugin = cspOrchestratorPluginMap.get(csp);
            List<CredentialType> availableCredentialTypes =
                    orchestratorPlugin.getAvailableCredentialTypes();
            List<AbstractCredentialInfo> credentialDefinitions =
                    orchestratorPlugin.getCredentialDefinitions();
            if (!Objects.isNull(availableCredentialTypes) && !Objects.isNull(
                    credentialDefinitions)) {
                Map<CredentialType, CredentialDefinition> credentialMap =
                        credentialDefinitions.stream()
                                .filter(credentialDefinition -> availableCredentialTypes.contains(
                                        credentialDefinition.getType()))
                                .collect(Collectors.toMap(AbstractCredentialInfo::getType,
                                        cd -> (CredentialDefinition) cd));
                deleteCredentialApi(csp);
                generateCredentialApi(credentialMap, csp);
            }
        });
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

    /**
     * create credentialApi for plugins.
     */
    public void createCredentialApi(Map<CredentialType, CredentialDefinition> credentialMap,
                                    Csp csp) {
        String yamlFileName = csp + "_" + "credentialApi" + ".yaml";
        String credentialApiDir = getCredentialApiDir();
        File dir = new File(credentialApiDir);
        if (!dir.exists()) {
            log.info("Create service openApi dir:{} success", credentialApiDir);
        }
        File yamlFile = new File(credentialApiDir, yamlFileName);
        File htmlFile = new File(credentialApiDir, csp + "_" + "credentialApi" + ".html");
        try {
            String apiDocsJson = getApiDocsJson(credentialMap, csp);
            try (FileWriter apiWriter = new FileWriter(yamlFile.getPath())) {
                apiWriter.write(apiDocsJson);
            }
            log.info("credentialApi yamlFile:{} creation successful.", yamlFile.getName());
            if (yamlFile.exists() && openApiUtil.downloadClientJar(credentialApiDir)) {
                File jarPath = new File(credentialApiDir, "openapi-generator-cli.jar");
                String comm = String.format("java -jar %s generate -g html2 "
                        + "-i %s -o %s", jarPath.getPath(), yamlFile.getPath(), credentialApiDir);
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
                    log.error("{} credentialApi html file creation failed." + stdErrOut, csp);
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
            log.error("credentialApi html file creation failed.", ex);
            throw new RuntimeException("credentialApi html file creation failed.", ex);
        } finally {
            // Delete the temp file named ${Csp}_credentialApi.yaml
            if (yamlFile.exists()) {
                if (yamlFile.delete()) {
                    log.info("credentialApi temp yaml file deletion successful.");
                }
            }
        }
    }

    /**
     * generate credentialApi for plugins.
     */
    @Async("taskExecutor")
    public void generateCredentialApi(
            Map<CredentialType, CredentialDefinition> credentialMap, Csp csp) {
        MDC.put(TASK_ID, UUID.randomUUID().toString());
        createCredentialApi(credentialMap, csp);
    }

    /**
     * delete credentialApi for plugins.
     */
    public void deleteCredentialApi(Csp csp) {
        File file = new File(getCredentialApiDir(), csp + "_" + "credentialApi" + ".html");
        if (file.exists()) {
            if (file.delete()) {
                log.info("credentialApi html file:{} deletion successful.", file.getName());
            }
        }
    }

    private Map<String, Map<String, String>> getVariablePropertiesMap(
            List<CredentialVariable> credentialVariables) {
        Map<String, Map<String, String>> variablePropertiesMap = new HashMap<>();
        for (CredentialVariable credentialVariable : credentialVariables) {
            Map<String, String> credentialVariableMap = new HashMap<>();
            credentialVariableMap.put("example", credentialVariable.getValue());
            credentialVariableMap.put("type", "string");
            credentialVariableMap.put("description", credentialVariable.getDescription());
            variablePropertiesMap.put(credentialVariable.getName(), credentialVariableMap);
        }
        return variablePropertiesMap;
    }

    private String getApiDocsJson(Map<CredentialType, CredentialDefinition> credentialMap,
                                  Csp csp) {
        String serviceUrl = getServiceUrl();
        String variableNamesStr = null;
        String variablePropertiesStr = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CredentialDefinition credentialDefinition = credentialMap.get(CredentialType.VARIABLES);
        List<CredentialVariable> variables = credentialDefinition.getVariables();
        List<String> variableNames = variables.stream().map(CredentialVariable::getName)
                .collect(Collectors.toList());
        Map<String, Map<String, String>> variablePropertiesMap =
                getVariablePropertiesMap(variables);
        String operationName = csp + "-" + credentialDefinition.getName();
        try {
            variableNamesStr = mapper.writeValueAsString(variableNames);
            variablePropertiesStr = mapper.writeValueAsString(variablePropertiesMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to write value as string.", e);
        }
        //CHECKSTYLE OFF: LineLength
        return String.format("""
                        openapi: 3.0.1
                        info:
                          title: %s plugin credentials definition
                          version: v1.16
                          description: CredentialApi document of user add credentials for %s plugin.
                        servers:
                          - url: %s
                            description: Generated server url
                        tags:
                          - name: Service Available
                            description: APIs to query the credential capabilities supported by CSP.
                        paths:
                          /xpanse/service/%s:
                            post:
                              tags:
                                - Service
                              description: %s credentialType supported by the %s plugin.
                              operationId: %s
                              requestBody:
                                content:
                                  application/json:
                                    schema:
                                      $ref: '#/components/schemas/%s'
                                required: true
                              responses:
                                '202':
                                  description: Accepted
                                  content:
                                    application/json:
                                      schema:
                                        type: string
                                        format: uuid
                                '400':
                                  description: Bad Request
                                  content:
                                    '*/*':
                                      schema:
                                        $ref: '#/components/schemas/Response'
                                '404':
                                  description: Not Found
                                  content:
                                    '*/*':
                                      schema:
                                        $ref: '#/components/schemas/Response'
                                '500':
                                  description: Internal Server Error
                                  content:
                                    '*/*':
                                      schema:
                                        $ref: '#/components/schemas/Response'
                        components:
                          schemas:
                            Response:
                              required:
                                - code
                                - message
                                - success
                              type: object
                              properties:
                                code:
                                  type: string
                                  description: The result code of response.
                                message:
                                  type: string
                                  description: The result message of response.
                                success:
                                  type: boolean
                                  description: The success boolean of response.
                            %s:
                              required: %s
                              type: object
                              properties: %s
                        """,
                csp, csp, serviceUrl, operationName, credentialDefinition.getName(),
                csp, operationName, credentialDefinition.getName(),
                credentialDefinition.getName(), variableNamesStr, variablePropertiesStr);
    }
}
