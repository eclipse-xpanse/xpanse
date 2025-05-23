/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.generate;

import static org.eclipse.xpanse.ai.generate.PromptTemplateVariables.APPLICATION_NAME_VAR;
import static org.eclipse.xpanse.ai.generate.PromptTemplateVariables.BACKEND_NAME_VAR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.ai.generate.structure.CodeGenerationResponseItem;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/** Client bean that interacts with an LLM provider. */
@Component
@Slf4j
@Profile("ai")
public class AiClientForCodeGeneration {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT_FILE = "system-prompt.txt";

    private final OpenAiChatModel chatModel;

    @Autowired
    public AiClientForCodeGeneration(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /** Uses prompt and generates code fils by using LLM. */
    public String generateCode(
            String promptFileLocation, String backendName, String applicationName)
            throws IOException {
        log.info("sending request to LLM");
        ChatOptions options = OpenAiChatOptions.builder().temperature(0.0).topP(1.0).build();
        ChatClient chatClient =
                ChatClient.builder(chatModel)
                        .defaultOptions(options)
                        .defaultAdvisors(new SimpleLoggerAdvisor())
                        .build();

        String response =
                chatClient
                        .prompt()
                        .system(new ClassPathResource(SYSTEM_PROMPT_FILE))
                        .user(
                                promptUserSpec ->
                                        promptUserSpec
                                                .text(new ClassPathResource(promptFileLocation))
                                                .params(
                                                        Map.of(
                                                                APPLICATION_NAME_VAR,
                                                                applicationName,
                                                                BACKEND_NAME_VAR,
                                                                backendName)))
                        .templateRenderer(
                                StTemplateRenderer.builder()
                                        .startDelimiterToken('<')
                                        .endDelimiterToken('>')
                                        .build())
                        .call()
                        .content();
        log.info("Original Response: {}", response);
        CodeGenerationResponseItem[] codeGenerationResponse =
                objectMapper.readValue(response, CodeGenerationResponseItem[].class);
        return processAndWriteToFiles(codeGenerationResponse);
    }

    private String processAndWriteToFiles(CodeGenerationResponseItem[] codeGenerationResponse)
            throws IOException {
        File directory =
                new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID());
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // mkdirs() creates parent directories as well
            if (created) {
                log.info("Directory {} created successfully", directory.getAbsolutePath());
            }
        }

        Arrays.stream(codeGenerationResponse)
                .forEach(
                        item -> {
                            String fileName = item.getFileName();
                            String content = item.getContent();
                            try {
                                writeToFile(directory.getAbsolutePath(), fileName, content);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        return directory.getAbsolutePath();
    }

    private void writeToFile(String directory, String fileName, String content) throws IOException {
        File file = new File(directory + File.separator + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            log.info("Written to: {}", fileName);
        }
    }

    private static String getSystemPrompt() {
        try {
            File file = ResourceUtils.getFile("classpath:system-prompt.txt");
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
