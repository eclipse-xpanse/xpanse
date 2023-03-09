/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.modules.models.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * The query model for search register services.
 */
@Data
@Valid
public class RegisteredServiceQuery {

    @Schema(description = "Name of the cloud service provider.")
    private Csp csp;

    @Schema(description = "Category of the service.")
    private Category category;

    @Schema(description = "Name of the registered service.")
    private String serviceName;

    @Schema(description = "Version of the registered service.")
    private String serviceVersion;

}
