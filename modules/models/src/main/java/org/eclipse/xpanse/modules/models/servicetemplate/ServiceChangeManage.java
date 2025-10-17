/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ControllerApiMethods;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ConfigurationManagerTool;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines the service config manage of the service. */
@Valid
@Data
@Slf4j
public class ServiceChangeManage {

    @NotNull
    @Schema(description = "the tool used to manage the service configuration.")
    private ConfigurationManagerTool type;

    @NotNull
    @Schema(description = "the version of the agent that will be used by service resources.")
    private String agentVersion;

    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The collection of the configuration manage script.")
    private List<ServiceChangeScript> configManageScripts;

    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The collection of service configuration parameters.")
    private List<ServiceChangeParameter> configurationParameters;

    @Schema(
            description =
                    "controller methods for service configuration. One method for changing service"
                            + " configuration and one method for reading current configuration.")
    private ControllerApiMethods controllerApiMethods;
}
