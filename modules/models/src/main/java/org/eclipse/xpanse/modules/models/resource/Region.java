/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * The regions of the Cloud Service Provider.
 */
@Data
public class Region {

    @NotNull
    @NotBlank
    @NotEmpty
    private String name;

    private String area;

}
