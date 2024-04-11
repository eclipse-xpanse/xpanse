/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 * Defines for service flavors.
 */
@Data
@ToString(callSuper = true)
public class Flavors {

    @NotNull
    @Schema(description = "The flavors of the managed service.")
    List<ServiceFlavor> serviceFlavors;


    @NotNull
    @Schema(description = "Impact on service when flavor is changed.")
    private ModificationImpact modificationImpact;
}
