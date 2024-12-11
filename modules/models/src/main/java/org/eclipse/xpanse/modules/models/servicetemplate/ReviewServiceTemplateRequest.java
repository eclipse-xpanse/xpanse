/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;

/**
 * Defines the review request model for service template registration.
 */
@Valid
@Data
@Slf4j
public class ReviewServiceTemplateRequest {

    @NotNull
    @Schema(description = "The result of review registration.")
    private ServiceReviewResult reviewResult;

    @Schema(description = "The comment of review registration.")
    private String reviewComment;
}
