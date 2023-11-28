/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.observability;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

/**
 * Configuration to workaround issues with ParameterNameDiscoverer bean which is instantiated by
 * multiple libraries. Hence, this class defines a primary bean which will solve the duplicate
 * bean exception.
 */
@Configuration
public class OpenTelemetryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Primary
    ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

}
