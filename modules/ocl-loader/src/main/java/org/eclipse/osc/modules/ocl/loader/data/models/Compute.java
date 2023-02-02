/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import java.util.List;
import lombok.Data;

/**
 * Defines compute services required to run the managed service.
 */
@Data
public class Compute {

    private List<Vm> vm;

}
