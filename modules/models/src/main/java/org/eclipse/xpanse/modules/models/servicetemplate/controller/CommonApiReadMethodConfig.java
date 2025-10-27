/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.CommonReadDomainType;

/**
 * Data model to describe common read methods. These are methods that are belong to specific domains
 * of xpanse but impacts more than one phase of service life cycle.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonApiReadMethodConfig extends ApiReadMethodConfig {

    @NotNull
    @Schema(description = "Read request type")
    private CommonReadDomainType readDomainType;
}
