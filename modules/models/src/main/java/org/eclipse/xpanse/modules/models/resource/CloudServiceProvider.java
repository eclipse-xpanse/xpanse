/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * Defines for the Cloud Service Provider.
 */
@Data
public class CloudServiceProvider {

    @NotNull
    @Schema(description = "The Cloud Service Provider. valid values: aws, azure, alibaba, huawei")
    private Csp name;

    @Valid
    @NotEmpty
    @Schema(description = "The regions of the Cloud Service Provider")
    private List<String> regions;
}
