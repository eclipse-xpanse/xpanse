/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;

/** Define autofill properties for deployment variables. */
@Data
public class AutoFill {

    @NotNull
    @Schema(description = "Type of the cloud resource to be reused.")
    private DeployResourceKind deployResourceKind;

    @NotNull
    @Schema(
            description =
                    " defines if the required cloud resource can be newly created "
                            + "or should the existing resources must only be used.")
    private Boolean isAllowCreate;
}
