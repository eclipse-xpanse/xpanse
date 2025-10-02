/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Defines for the output variable of helm. */
@EqualsAndHashCode(callSuper = true)
@Data
public class HelmOutputVariable extends OutputVariable {

    @Schema(description = "The type of kubernetes resource")
    private String resourceType;
}
