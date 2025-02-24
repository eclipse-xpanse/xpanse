/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Data;

/** Details of the service configuration. */
@Data
public class ServiceConfigurationDetails {

    @Schema(
            description = "Defines the current configuration of the service.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> configuration;

    @Schema(description = "Timestamp when the configuration was first created.")
    private OffsetDateTime createdTime;

    @Schema(description = "Timestamp when the configuration was last updated.")
    private OffsetDateTime updatedTime;
}
