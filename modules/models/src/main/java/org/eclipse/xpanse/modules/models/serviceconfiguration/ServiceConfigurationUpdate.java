/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

/** ServiceConfigurationUpdate model. */
@Data
public class ServiceConfigurationUpdate {

    @NotNull
    @Size(min = 1)
    @Schema(description = "The service configuration to be modified")
    private Map<String, Object> configuration;
}
