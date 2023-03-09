/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.view;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;

/**
 * Define view object for UI Client query registered service by category.
 */
@Data
public class ServiceVo {

    @Schema(description = "The ID of the service")
    private UUID id;

    /**
     * The category of the Service.
     */
    @Schema(description = "The catalog of the service")
    private Category category;

    /**
     * The name of the Service.
     */
    @Schema(description = "The name of the service")
    private String name;

    /**
     * The version of the Service.
     */
    @Schema(description = "The version of the service")
    private String version;

    /**
     * The csp of the Service.
     */
    @Schema(description = "The provider of the service")
    private Csp csp;

    /**
     * The flavor of the Service.
     */
    @Schema(description = "The flavor of the service")
    private String flavor;

    /**
     * The state of the Service.
     */
    @Schema(description = "The state of the service")
    private ServiceState serviceState;
}
