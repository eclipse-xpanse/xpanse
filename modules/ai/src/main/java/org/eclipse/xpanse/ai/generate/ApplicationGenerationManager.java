/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.generate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.ai.docker.DockerImageManage;
import org.eclipse.xpanse.modules.models.ai.enums.AiApplicationType;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/** Wrapper class which connects to LLM, generates code and creates code files on filesystem. */
@Slf4j
@Component
@Profile("ai")
public class ApplicationGenerationManager {

    private static final String TEMPLATES_FOLDER = "templates";
    private final org.eclipse.xpanse.ai.generate.AiClientForCodeGeneration clientForCodeGeneration;

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
    public String generateMcpServerImage(AiApplicationType aiApplicationType)
            throws IOException, InterruptedException {
        String codePath = clientForCodeGeneration.generateCode(readFile(aiApplicationType));
        return dockerImageManage.createAndPushImage(codePath, aiApplicationType.toValue());
    }

    /**
     * Reads a file from the classpath and returns the content as a string. The file path is
     * constructed as follows: {@code TEMPLATES_FOLDER}/{@code aiApplicationType.toValue()}/{@code
     * aiApplicationType.toValue()}-prompt. If the file does not exist or can not be read for any
     * reason, an {@link XpanseUnhandledException} is thrown.
     *
     * @param aiApplicationType the type of AI application to read the prompt file for
     * @return the content of the file as a string
     */
    private static String readFile(AiApplicationType aiApplicationType) {

        try {
            File file =
                    ResourceUtils.getFile(
                            "classpath:"
                                    + String.format(
                                            "%s/%s/%s-prompt",
                                            TEMPLATES_FOLDER,
                                            aiApplicationType.toValue(),
                                            aiApplicationType.toValue()));
            if (!file.exists()) {
                throw new XpanseUnhandledException("Could not find file: " + file.getName());
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes);
        } catch (IOException e) {
            throw new XpanseUnhandledException(e.getMessage());
        }
    }
}
