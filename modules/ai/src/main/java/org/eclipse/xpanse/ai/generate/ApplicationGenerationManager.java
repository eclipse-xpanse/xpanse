/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.generate;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.ai.docker.DockerImageManage;
import org.eclipse.xpanse.modules.models.ai.enums.AiApplicationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Wrapper class which connects to LLM, generates code and creates code files on filesystem. */
@Slf4j
@Component
@Profile("ai")
public class ApplicationGenerationManager {

    private final AiClientForCodeGeneration clientForCodeGeneration;

    private final DockerImageManage dockerImageManage;

    /** Constructor method. */
    @Autowired
    public ApplicationGenerationManager(
            AiClientForCodeGeneration clientForCodeGeneration,
            DockerImageManage dockerImageManage) {
        this.clientForCodeGeneration = clientForCodeGeneration;
        this.dockerImageManage = dockerImageManage;
    }

    /** Generates code files and build a docker image for it. */
    public String generateApplicationServerImage(AiApplicationType aiApplicationType)
            throws IOException, InterruptedException {
        String codePath =
                clientForCodeGeneration.generateCode(
                        userPromptFileName(aiApplicationType),
                        getBackendName(aiApplicationType),
                        aiApplicationType.toValue());
        return dockerImageManage.createAndPushImage(codePath, aiApplicationType.toValue());
    }

    private static String userPromptFileName(AiApplicationType aiApplicationType) {
        return switch (aiApplicationType) {
            case GAUSSDB_MCP, MYSQL_MCP -> "templates/mcp/prompts/sql-database/user-prompt.txt";
        };
    }

    private String getBackendName(AiApplicationType aiApplicationType) {
        return switch (aiApplicationType) {
            case GAUSSDB_MCP -> "OpenGauss";
            case MYSQL_MCP -> "MySQL";
        };
    }
}
