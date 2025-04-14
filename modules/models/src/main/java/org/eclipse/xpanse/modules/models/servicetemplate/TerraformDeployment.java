/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.validation.annotation.Validated;

/** Defines the TerraformDeployment. */
@EqualsAndHashCode
@Data
@Validated
public class TerraformDeployment implements Serializable {

    @Serial private static final long serialVersionUID = 2566478948717883360L;

    @Schema(
            description =
                    "Deployment scripts stored in a Map. file name as the key and content as the"
                            + " value. Either scriptFiles or scriptsRepo must be provided.")
    private Map<String, String> scriptFiles;

    @Schema(
            description =
                    "Deployment scripts hosted on a GIT repo. "
                            + "Either scriptFiles or scriptsRepo must be provided.")
    private ScriptsRepo scriptsRepo;

    @Valid
    @NotNull
    @NotEmpty
    @UniqueElements
    @Schema(
            description =
                    "The input variables for the deployment, which will be passed to the"
                        + " deployer.The list elements must be unique. All input variables are put"
                        + " together to build a JSON 'object' with each variable as a property of"
                        + " this object.")
    private List<InputVariable> inputVariables;

    @Valid
    @NotNull
    @NotEmpty
    @UniqueElements
    @Schema(
            description =
                    "The output variables for the deployment, which will be generated by the"
                            + " deployer. The list elements must be unique.")
    private List<OutputVariable> outputVariables;
}
