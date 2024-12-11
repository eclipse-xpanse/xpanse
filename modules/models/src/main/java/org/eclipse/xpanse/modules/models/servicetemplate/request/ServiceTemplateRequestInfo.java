/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Defines view object for service template request info. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceTemplateRequestInfo {

    @NotNull
    @Schema(description = "ID of the service template.")
    private UUID serviceTemplateId;

    @NotNull
    @Schema(description = "ID of the request history of the service template.")
    private UUID requestId;

    @NotNull
    @Schema(description = "If the request is submitted for review.")
    private boolean isRequestSubmittedForReview;
}
