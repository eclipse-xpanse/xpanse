/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.modules.models.view;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * Define view object for UI Client query registered service by category.
 */
@Data
public class CategoryOclVo {

    @Schema(description = "Name of the registered service.")
    private String name;

    @Schema(description = "List of the registered service group by service version.")
    private List<VersionOclVo> versions;
}
