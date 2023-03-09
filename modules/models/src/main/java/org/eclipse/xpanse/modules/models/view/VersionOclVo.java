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
 * Define view object for UI Client query registered service by service version.
 */
@Data
public class VersionOclVo {

    @Schema(description = "Version of the registered service.")
    private String version;

    @Schema(description = "List of the registered services group by service version.")
    private List<ProviderOclVo> cloudProvider;

}
