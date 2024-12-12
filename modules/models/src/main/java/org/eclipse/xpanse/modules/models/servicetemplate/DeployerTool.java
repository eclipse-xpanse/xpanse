/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.validation.annotation.Validated;

/** Defines the Deployer Tool. */
@Data
@Validated
public class DeployerTool implements Serializable {

    /** Deployer version required version regex. */
    @Hidden @JsonIgnoreProperties
    public static final String DEPLOYER_TOOL_REQUIRED_VERSION_REGEX =
            "^(=|>=|<=)\\s*[vV]?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    @Serial private static final long serialVersionUID = 163119346767744353L;

    @NotNull
    @Schema(description = "The type of the deployer which will handle the service deployment.")
    private DeployerKind kind;

    @NotNull
    @NotBlank
    @Pattern(regexp = DEPLOYER_TOOL_REQUIRED_VERSION_REGEX)
    @Schema(description = "The version of the deployer which will handle the service deployment.")
    private String version;
}
