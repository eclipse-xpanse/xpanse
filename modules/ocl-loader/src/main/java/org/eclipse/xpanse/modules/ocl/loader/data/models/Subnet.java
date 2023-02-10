/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.loader.data.utils.Cidr;

/**
 * Subnet information on which the managed service will be deployed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Subnet extends RuntimeBase {

    @NotBlank
    @Schema(description = "The vpc which the subnet belongs to")
    private String vpc;

    @NotBlank
    @Schema(description = "The name of the subnet")
    private String name;

    @Cidr
    @Schema(description = "The cidr of the subnet, for example: 192.168.9.0/24")
    private String cidr;

}
