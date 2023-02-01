/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator;

import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DefaultOrchestratorStorageBean {

    @Bean
    @ConditionalOnMissingBean(OrchestratorStorage.class)
    public FileOrchestratorStorage fileOrchestratorStorage(Environment environment)
            throws IOException {
        return new FileOrchestratorStorage(environment);
    }
}
