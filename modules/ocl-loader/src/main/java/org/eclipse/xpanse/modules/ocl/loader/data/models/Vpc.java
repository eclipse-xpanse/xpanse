/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.loader.data.utils.Cidr;

/**
 * Defines Virtual Private Cloud configuration for the managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Vpc extends RuntimeBase {

    @Schema(description = "The name of the vpc")
    private String name;

    @Cidr
    @Schema(description = "The cidr of the vpc, for example: 192.168.1.0/24")
    private String cidr;

}
