/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.logging;

import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultSink;

/** Bean to auto configure Logbook configuration. */
@Configuration
public class LogbookConfiguration {

    private final HttpServerRequestLoggingProperties httpServerRequestLoggingProperties;

    @Autowired
    public LogbookConfiguration(
            HttpServerRequestLoggingProperties httpServerRequestLoggingProperties) {
        this.httpServerRequestLoggingProperties = httpServerRequestLoggingProperties;
    }

    /** Method to instantiate Logbook bean. */
    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .correlationId(new CustomRequestIdGenerator())
                .condition(exclude(getExcludedUris()))
                .sink(
                        new DefaultSink(
                                new CustomHttpLogFormatter(),
                                new CustomHttpLogWriter(
                                        httpServerRequestLoggingProperties.getEnabled())))
                .build();
    }

    private List<Predicate<HttpRequest>> getExcludedUris() {
        List<Predicate<HttpRequest>> predicates = new ArrayList<>();
        for (String excludedUri : httpServerRequestLoggingProperties.getExcludeUri()) {
            predicates.add(requestTo(excludedUri));
        }
        return predicates;
    }
}
