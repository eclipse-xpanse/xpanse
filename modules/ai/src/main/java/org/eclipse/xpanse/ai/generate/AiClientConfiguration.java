/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.generate;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Client configuration to connect to LLM. */
@Configuration
@Slf4j
@Profile("ai")
public class AiClientConfiguration {

    private final String llmUrl;

    private final String llmModelName;

    private final String llmKey;

    /** Bean to configure LLM client. */
    public AiClientConfiguration(
            @Value("${llm.provider.api.endpoint.url}") String llmUrl,
            @Value("${llm.provider.model.name}") String llmModelName,
            @Value("${llm.provider.api.key}") String llmKey) {
        this.llmUrl = llmUrl;
        this.llmModelName = llmModelName;
        this.llmKey = llmKey;
    }

    /** Initializes connection to LLM provider. */
    @Bean
    public OpenAiApi chatCompletionApi() {
        return OpenAiApi.builder()
                .baseUrl(llmUrl)
                .apiKey(Objects.nonNull(llmKey) ? new SimpleApiKey(llmKey) : new NoopApiKey())
                .build();
    }

    /** Configures the LLM model and its options. */
    @Bean
    public OpenAiChatModel openAiClient(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .toolCallingManager(ToolCallingManager.builder().build())
                .defaultOptions(OpenAiChatOptions.builder().model(llmModelName).build())
                .build();
    }
}
