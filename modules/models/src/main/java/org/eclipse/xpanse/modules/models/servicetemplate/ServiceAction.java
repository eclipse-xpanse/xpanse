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
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ConfigurationManagerTool;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines the service action of the service. */
@Valid
@Data
@Slf4j
public class ServiceAction {

    @NotNull
    @Schema(description = "the name of service action.")
    private String name;

    @NotNull
    @Schema(description = "the tool used to manage the service action.")
    private ConfigurationManagerTool type;

    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The collection of the action manage script.")
    private List<ServiceChangeScript> actionManageScripts;

    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The collection of service action parameters.")
    private List<ServiceChangeParameter> actionParameters;
}
