/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.servicetemplate.change;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines view object for service template request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceTemplateChangeInfo {

    @NotNull
    @Schema(description = "ID of the registered service template.")
    private UUID serviceTemplateId;

    @NotNull
    @Schema(description = "ID of the change history of the service template.")
    private UUID changeId;
}
