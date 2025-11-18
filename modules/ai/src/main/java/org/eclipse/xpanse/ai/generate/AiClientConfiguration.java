/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.generate;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.ai.config.AiProperties;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Client configuration to connect to LLM. */
@Configuration
@Slf4j
@Profile("ai")
public class AiClientConfiguration {

    private final AiProperties aiProperties;

    /** Bean to configure LLM client. */
    public AiClientConfiguration(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /** Initializes connection to LLM provider. */
    @Bean
    public OpenAiApi chatCompletionApi() {
        return OpenAiApi.builder()
                .baseUrl(aiProperties.getLlm().getProviderApiEndpointUrl())
                .apiKey(
                        Objects.nonNull(aiProperties.getLlm().getProviderApiKey())
                                ? new SimpleApiKey(aiProperties.getLlm().getProviderApiKey())
                                : new NoopApiKey())
                .build();
    }

    /** Configures the LLM model and its options. */
    @Bean
    public OpenAiChatModel openAiClient(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .toolCallingManager(ToolCallingManager.builder().build())
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model(aiProperties.getLlm().getModelName())
                                .build())
                .build();
    }
}
