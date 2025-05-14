/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.generate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Client bean that interacts with an LLM provider. */
@Component
@Slf4j
@Profile("ai")
public class AiClientForCodeGeneration {

    private static final String SYSTEM_PROMPT =
            """
                you are a code generator.
                You must generate fully usable code and leave nothing for the user to implement.
                Output must contain only code. No examples, explanation, comments needed.
            """;

    private final OpenAiChatModel chatModel;

    @Autowired
    public AiClientForCodeGeneration(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /** Uses prompt and generates code fils by using LLM. */
    public String generateCode(String prompt) throws IOException {
        log.info("sending request to LLM");
        ChatOptions options = OpenAiChatOptions.builder().temperature(0.0).topP(1.0).build();
        ChatClient chatClient =
                ChatClient.builder(chatModel)
                        .defaultOptions(options)
                        .defaultAdvisors(new SimpleLoggerAdvisor())
                        .build();
        SystemMessage systemMessage = new SystemMessage(SYSTEM_PROMPT);
        UserMessage userMessage = new UserMessage(prompt);
        String response =
                chatClient.prompt(new Prompt(List.of(systemMessage, userMessage))).call().content();
        log.info("Original Response: {}", response);
        return processAndWriteToFiles(response);
    }

    private String processAndWriteToFiles(String input) throws IOException {
        // Split the input into lines
        String removeSpaces = input.trim();
        String[] lines = removeSpaces.split("\n");

        String currentFileName = null;
        StringBuilder currentBlock = new StringBuilder();
        boolean insideCodeBlock = false;
        File directory =
                new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID());

        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // mkdirs() creates parent directories as well
            if (created) {
                log.info("Directory created successfully");
            }
        }

        for (String line : lines) {
            // Check if the line starts with '## ' (this marks a file name)
            if (line.startsWith("## ")) {
                // If we already have a file to write, write the previous one
                if (currentFileName != null && !currentBlock.isEmpty()) {
                    writeToFile(
                            directory.getAbsolutePath(), currentFileName, currentBlock.toString());
                }
                currentFileName = line.substring(3).trim(); // Extract after "## "
                currentBlock = new StringBuilder();
                insideCodeBlock = false;
            } else if (line.startsWith("# ")) {
                // If we already have a file to write, write the previous one
                if (currentFileName != null && !currentBlock.isEmpty()) {
                    writeToFile(
                            directory.getAbsolutePath(), currentFileName, currentBlock.toString());
                }
                currentFileName = line.substring(2).trim(); // Extract after "# "
                currentBlock = new StringBuilder();
                insideCodeBlock = false;
            } else if (line.startsWith("```")) {
                insideCodeBlock = !insideCodeBlock;
            } else {
                if (insideCodeBlock) {
                    currentBlock.append(line).append("\n");
                }
            }
        }

        // Write the last block to file
        if (currentFileName != null && !currentBlock.isEmpty()) {
            writeToFile(directory.getAbsolutePath(), currentFileName, currentBlock.toString());
        }

        return directory.getAbsolutePath();
    }

    private void writeToFile(String directory, String fileName, String content) throws IOException {
        File file = new File(directory + File.separator + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            log.info("Written to: {}", fileName);
        }
    }
}
