/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.view;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;

/**
 * Define view object for UI Client query registered service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OclDetailVo extends Ocl {

    @Schema(description = "ID of the registered service.")
    private UUID id;

}
