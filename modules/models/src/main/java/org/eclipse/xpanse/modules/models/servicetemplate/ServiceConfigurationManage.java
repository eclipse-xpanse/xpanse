/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ConfigurationManagerTool;
import org.hibernate.validator.constraints.UniqueElements;

/**
 * Defines the service config manage of the service.
 */

@Valid
@Data
@Slf4j
public class ServiceConfigurationManage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(description = "the tool used to manage the service configuration.")
    private ConfigurationManagerTool type;

    @NotNull
    @Schema(description = "the version of the agent that will be used by service resources.")
    private String agentVersion;

    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The collection of the configuration manage script.")
    private List<ConfigManageScript> configManageScripts;

    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The collection of service configuration parameters.")
    private List<ServiceConfigurationParameter> configurationParameters;


}
