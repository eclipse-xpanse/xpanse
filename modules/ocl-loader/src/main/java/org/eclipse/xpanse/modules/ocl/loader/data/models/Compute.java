/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import jakarta.validation.constraints.Size;
import java.util.List;
import jdk.jfr.Description;
import lombok.Data;

/**
 * Defines compute services required to run the managed service.
 */
@Data
public class Compute {

    @Size(min = 1)
    @Description("The VMs for the managed service")
    private List<Vm> vms;

}
