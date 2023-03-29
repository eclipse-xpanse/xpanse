/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Description;
import lombok.Data;

/**
 * The regions of the Cloud Service Provider.
 */
@Data
public class Region {

    @NotNull
    @NotBlank
    @NotEmpty
    @Description("The name of the Region")
    private String name;

    @NotEmpty
    @NotBlank
    @Description("The area which the region belongs to, such as Asia, Europe, Africa")
    private String area = "Others";
}
