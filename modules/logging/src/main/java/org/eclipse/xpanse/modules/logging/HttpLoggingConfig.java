/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

// HttpLoggingConfig.java

package org.eclipse.xpanse.modules.logging;

import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** Reads HTTP logging related configuration from spring context. */
@Getter
@Configuration
public class HttpLoggingConfig {

    private final boolean isHttpLoggingEnabled;
    private final List<String> excludedUris;

    public HttpLoggingConfig(
            @Value("${http.logging.enabled:true}") boolean isHttpLoggingEnabled,
            @Value("${http.logging.exclude.uri:}#{T(java.util.Collections).emptyList()}")
                    List<String> excludedUris) {
        this.isHttpLoggingEnabled = isHttpLoggingEnabled;
        this.excludedUris = excludedUris;
    }
}
