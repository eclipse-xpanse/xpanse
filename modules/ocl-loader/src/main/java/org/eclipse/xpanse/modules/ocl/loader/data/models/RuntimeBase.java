/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.RuntimeState;

/**
 * RuntimeBase class.
 */
@Data
public class RuntimeBase {

    @Hidden
    private RuntimeState state = RuntimeState.INACTIVE;
    @Hidden
    String id = "";

}
