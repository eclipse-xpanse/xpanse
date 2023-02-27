/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * Defines compute services required to run the managed service.
 */
@Data
public class Compute {

    @NotEmpty
    @Schema(description = "The VMs for the managed service")
    private List<Vm> vms;

}
